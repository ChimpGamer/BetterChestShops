package nl.chimpgamer.betterchestshops.paper.listeners

import com.Acrobot.Breeze.Utils.MaterialUtil
import com.Acrobot.Breeze.Utils.PriceUtil
import com.Acrobot.ChestShop.Events.PreShopCreationEvent
import com.Acrobot.ChestShop.Events.ShopCreatedEvent
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent
import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.Acrobot.ChestShop.Utils.uBlock
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import java.math.BigDecimal
import java.util.logging.Level

class ChestShopListener(private val plugin: BetterChestShopsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPreShopCreation(event: PreShopCreationEvent) {
        val player = event.player
        if (event.outcome === PreShopCreationEvent.CreationOutcome.NOT_ENOUGH_MONEY) return

        if (!ChestShopSign.isAdminShop(event.sign) && plugin.hasReachedLimit(player)) {
            event.outcome = PreShopCreationEvent.CreationOutcome.NO_PERMISSION
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onShopCreate(event: ShopCreatedEvent) {
        try {
            val player = event.player

            val playerName = event.signLines[0]
            val amount = event.signLines[1].toInt()
            val prices = event.signLines[2]
            val itemLine = event.signLines[3]

            val itemStack = MaterialUtil.getItem(itemLine)
            val uuid = when {
                ChestShopSign.isAdminShop(playerName) -> plugin.consoleUUID
                playerName.isEmpty() -> player.uniqueId
                else -> event.ownerAccount?.uuid
            }

            if (uuid == null) {
                event.sign.block.breakNaturally()
                player.sendRichMessage("<red>Something went wrong while creating your shop.")
                plugin.logger.warning("NULL uuid ShopCreatedEvent for player: `$playerName`")
                return
            }
            val container = event.container
            if (container == null) {
                event.sign.block.breakNaturally()
                player.sendRichMessage("<red>Something went wrong while creating your shop.")
                plugin.logger.warning("NULL container ShopCreatedEvent for player: `$playerName`")
                return
            }

            val containerType = try {
                ContainerType.valueOf(container.type.toString().uppercase())
            } catch (ex: IllegalArgumentException) {
                player.sendRichMessage(
                    "<red>Unknown container type. Use one of the following container types: " + ContainerType.entries
                        .joinToString()
                )
                return
            }

            var buyPrice: BigDecimal? = null
            var sellPrice: BigDecimal? = null
            if (PriceUtil.hasBuyPrice(prices)) {
                buyPrice = PriceUtil.getExactBuyPrice(prices)
            }

            if (PriceUtil.hasSellPrice(prices)) {
                sellPrice = PriceUtil.getExactSellPrice(prices)
            }

            val chestShop = plugin.chestShopsHandler.addChestShop(
                uuid,
                containerType,
                amount,
                event.sign.location,
                itemStack,
                buyPrice,
                sellPrice
            )

            chestShop.spawnItem()
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "ERROR", ex)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onShopDestroy(event: ShopDestroyedEvent) {
        val chestShop =
            plugin.chestShopsHandler.getByLocation(event.sign.location) ?: return

        plugin.chestShopsHandler.removeChestShop(chestShop)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onBlockBreak(event: BlockBreakEvent) {
        // Omdat bij het breken van Container ShopDestroyedEvent niet wordt getriggerd.
        if (event.block.state is Container) {
            val sign = uBlock.findAnyNearbyShopSign(event.block) ?: return
            val chestShop = plugin.chestShopsHandler.getByLocation(sign.location) ?: return

            plugin.chestShopsHandler.removeChestShop(chestShop)
        }
    }

    /**
     * Protect hologram against water
     */
    @EventHandler(ignoreCancelled = true)
    fun onWaterFlow(event: BlockFromToEvent) {
        val sign = uBlock.findAnyNearbyShopSign(event.toBlock.getRelative(BlockFace.DOWN)) ?: return
        val chestShop = plugin.chestShopsHandler.getByLocation(sign.location)
        if (chestShop != null) event.isCancelled = true
    }

    /**
     * Remove chest shop item above chest shop when block is placed above chest.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val sign = uBlock.findAnyNearbyShopSign(event.block.getRelative(BlockFace.DOWN)) ?: return
        plugin.chestShopsHandler.getByLocation(sign.location)?.destroyItem()
    }

    /**
     * Spawn item above chest when block above chest shop is broken.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    suspend fun onBlockBreakAboveChest(event: BlockBreakEvent) {
        val block = event.block.getRelative(BlockFace.DOWN)
        val containerBlockState = block.state
        if (containerBlockState is Container) {
            val sign = uBlock.findAnyNearbyShopSign(block) ?: return
            // Delay by 5 ticks because it otherwise still thinks the block is there.
            delay(5.ticks)
            plugin.chestShopsHandler.getByLocation(sign.location)?.spawnItem()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (event.isNewChunk) return
        // Load chestshop items if there are chestshops in this chunk.

        plugin.chestShopsHandler.getChestShops().forEach { chestShop ->
            // Check if world is loaded and check if the chunk is loaded.
            val location = chestShop.signLocation
            if (location.isWorldLoaded && location.isChunkLoaded && location.chunk == event.chunk) {
                chestShop.spawnItem()
            }
        }
    }
}