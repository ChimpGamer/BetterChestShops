package nl.chimpgamer.betterchestshops.paper.handlers

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import nl.chimpgamer.betterchestshops.paper.storage.entities.BetterChestShopEntity
import nl.chimpgamer.betterchestshops.paper.storage.entities.ChestShopEntity
import nl.chimpgamer.betterchestshops.paper.storage.entities.toChestShop
import nl.chimpgamer.betterchestshops.paper.storage.tables.ChestShopsTable
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ChestShopsHandler(private val plugin: BetterChestShopsPlugin) {
    private val chestShops: MutableMap<Location, ChestShop> = ConcurrentHashMap()

    private val databaseDispatcher get() = plugin.databaseHandler.databaseDispatcher

    fun load() {
        val loadedChestShops = HashMap<Location, ChestShop>()
        transaction {
            val betterChestShops = BetterChestShopEntity.all()
            plugin.logger.info("Found ${betterChestShops.count()} better chestshops in the database!")
            if (betterChestShops.empty()) {
                plugin.dataFolder.resolve("data.db").copyTo(plugin.dataFolder.resolve("data.db.backup"), overwrite = true)
                val newChestShops = HashSet<BetterChestShopEntity>()
                // Do conversion!
                val oldChestShops = ChestShopEntity.all().map { it.toChestShop() }
                for (oldChestShop in oldChestShops) {
                    newChestShops.add(
                        BetterChestShopEntity.new {
                            this.creatorUUID = oldChestShop.creatorUUID
                            this.containerType = oldChestShop.containerType
                            this.amount = oldChestShop.amount
                            this.signLocation = oldChestShop.signLocation
                            this.itemStack = oldChestShop.itemStack
                            this.buyPrice = oldChestShop.buyPrice
                            this.sellPrice = oldChestShop.sellPrice
                            this.created = oldChestShop.created
                        }
                    )
                }
                loadedChestShops.putAll(newChestShops.map { it.toChestShop() }
                    .filter { runCatching { it.signLocation }.isSuccess } // If World is valid then cache it.
                    .map { it.signLocation to it })
                plugin.logger.info("Converted ${newChestShops.size}/${oldChestShops.size} chestshops!")
                transaction {
                    SchemaUtils.drop(ChestShopsTable)
                }
            } else {
                loadedChestShops.putAll(betterChestShops.map { it.toChestShop() }
                    .filter { runCatching { it.signLocation }.isSuccess } // If World is valid then cache it.
                    .map { it.signLocation to it })
            }
        }
        chestShops.clear()
        chestShops.putAll(loadedChestShops)
        plugin.logger.info("Loaded ${chestShops.size} chestshops from the database.")
    }

    fun getByLocation(location: Location) = chestShops[location]

    fun getAllByCreator(creator: UUID) = chestShops.values.filter { it.creatorUUID == creator }.toList()

    fun getCountByCreator(creator: UUID) = chestShops.filterValues { it.creatorUUID == creator }.count()

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
            val updatedChestShop = newSuspendedTransaction(databaseDispatcher) {
                BetterChestShopEntity[existingChestShop.id].apply {
                    this.creatorUUID = creator
                    this.amount = amount
                    this.itemStack = itemStack
                    this.buyPrice = buyPrice
                    this.sellPrice = sellPrice
                }
            }
            return chestShops.replace(signLocation, updatedChestShop.toChestShop())!!
        }
        val newChestShop = newSuspendedTransaction(databaseDispatcher) {
            BetterChestShopEntity.new {
                this.creatorUUID = creator
                this.containerType = containerType
                this.amount = amount
                this.signLocation = signLocation
                this.itemStack = itemStack
                this.buyPrice = buyPrice
                this.sellPrice = sellPrice
                this.created = LocalDateTime.now()
            }
        }
        //println("newChestShop=$newChestShop")
        //println("itemStack=${newChestShop.itemStack}")
        //println("itemStack.bytes=${newChestShop.itemStack?.bytes}")
        //val isnull = newChestShop.itemStack == null
        //val size = newChestShop.itemStack?.bytes?.size
        //println(isnull)
        //println(size)
        //println(newChestShop.itemStack?.bytes?.size)

        // For some reason size is 0 but the next time
        // bytes is called it is loaded? Hacky fix for error.
        //if (size == 0) {}
        val chestShop = newChestShop.toChestShop()
        chestShops[signLocation] = chestShop
        return chestShop
    }

    suspend fun removeChestShop(chestShop: ChestShop) {
        chestShop.destroyItem()
        val id = chestShop.id

        newSuspendedTransaction(databaseDispatcher) {
            BetterChestShopEntity.findById(id)?.delete()
        }
        chestShops.remove(chestShop.signLocation)
    }

    suspend fun removeChestShops(chestShops: Collection<ChestShop>): AtomicInteger {
        val count = AtomicInteger()
        newSuspendedTransaction(databaseDispatcher) {
            chestShops.forEach { BetterChestShopEntity.findById(it.id)?.delete(); if (this@ChestShopsHandler.chestShops.remove(it.signLocation, it)) count.incrementAndGet() }
        }
        return count
    }

    fun getChestShop(location: Location) = chestShops[location]

    fun getChestShopCount() = chestShops.count()

    fun getChestShops(): Collection<ChestShop> = chestShops.values

    fun getChestShopsUnordered() = chestShops.values.toSet()

    fun getChunkWithChestShops() = chestShops.entries.groupBy({ it.key.chunk }, { it.value })

    /**
     * Get the chest shops by the give chunk.
     * Checks if the world and chunk is loaded and if the chuck matches the given chunk
     *
     * @param chunk The chunk you want to get the chest shops from.
     * @return A set with all the chest shops in the given chunk.
     */
    fun getChestShopsByChunk(chunk: Chunk) = chestShops.filterKeys { it.isWorldLoaded && it.isChunkLoaded && it.chunk == chunk }.values.toSet()

    fun getChestShops(predicate: (ChestShop) -> Boolean) = chestShops.values.filter(predicate)
}