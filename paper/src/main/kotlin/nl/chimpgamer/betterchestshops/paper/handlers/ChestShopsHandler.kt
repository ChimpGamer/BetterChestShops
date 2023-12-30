package nl.chimpgamer.betterchestshops.paper.handlers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import nl.chimpgamer.betterchestshops.paper.storage.entities.ChestShopEntity
import nl.chimpgamer.betterchestshops.paper.storage.entities.toChestShop
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ChestShopsHandler(private val plugin: BetterChestShopsPlugin) {
    private val chestShops: MutableMap<Location, ChestShop> = ConcurrentHashMap()

    fun load() {
        val loadedChestShops = HashMap<Location, ChestShop>()
        transaction {
            loadedChestShops.putAll(ChestShopEntity.all().map { it.toChestShop() }
                .filter { runCatching { it.signLocation }.isSuccess } // If World is valid then cache it.
                .map { it.signLocation to it })
        }
        chestShops.clear()
        chestShops.putAll(loadedChestShops)
        plugin.logger.info("Loaded ${chestShops.size} chestshops from the database.")
    }

    fun getByLocation(location: Location) = chestShops[location]

    fun getAllByCreator(creator: UUID) = chestShops.values.filter { it.creatorUUID == creator }.toList()

    suspend fun addChestShop(
        creator: UUID,
        containerType: ContainerType,
        amount: Int,
        signLocation: Location,
        itemStack: ItemStack? = null,
        buyPrice: BigDecimal? = null,
        sellPrice: BigDecimal? = null
    ) : ChestShop {
        val existingChestShop = getByLocation(signLocation)
        if (existingChestShop != null) {
            // Okay so it is being updated?
            val updatedChestShop = withContext(Dispatchers.IO) {
                transaction {
                    ChestShopEntity[existingChestShop.id].apply {
                        this.creatorUUID = creator
                        this.amount = amount
                        this.itemStack = if (itemStack == null) null else ExposedBlob(itemStack.serializeAsBytes())
                        this.buyPrice = buyPrice
                        this.sellPrice = sellPrice
                    }
                }
            }
            return chestShops.replace(signLocation, updatedChestShop.toChestShop())!!
        } else {
            val newChestShop = withContext(Dispatchers.IO) {
                transaction {
                    ChestShopEntity.new {
                        this.creatorUUID = creator
                        this.containerType = containerType
                        this.amount = amount
                        this.world = signLocation.world.name
                        this.x = signLocation.blockX
                        this.y = signLocation.blockY
                        this.z = signLocation.blockZ
                        this.itemStack = if (itemStack == null) null else ExposedBlob(itemStack.serializeAsBytes())
                        this.buyPrice = buyPrice
                        this.sellPrice = sellPrice
                        this.created = LocalDateTime.now()
                    }
                }
            }
            //println("newChestShop=$newChestShop")
            println("itemStack=${newChestShop.itemStack?.bytes}")
            val chestShop = newChestShop.toChestShop()
            chestShops[signLocation] = chestShop
            return chestShop
        }
    }

    suspend fun removeChestShop(chestShop: ChestShop) {
        chestShop.destroyItem()

        val id = chestShop.id
        withContext(Dispatchers.IO) {
            transaction {
                ChestShopEntity[id].delete()
            }
        }
        chestShops.remove(chestShop.signLocation)
    }

    suspend fun removeChestShops(chestShops: Collection<ChestShop>): AtomicInteger {
        val count = AtomicInteger()
        withContext(Dispatchers.IO) {
            transaction {
                chestShops.forEach { ChestShopEntity[it.id].delete(); if (this@ChestShopsHandler.chestShops.remove(it.signLocation, it)) count.incrementAndGet() }
            }
        }
        return count
    }

    fun getChestShops() = chestShops.values.toList()

    fun getChestShopsUnordered() = chestShops.values.toSet()

    fun getChestShops(predicate: (ChestShop) -> Boolean) = chestShops.values.filter(predicate)
}