package nl.chimpgamer.betterchestshops.paper.models

import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.Acrobot.ChestShop.Utils.uBlock
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.capitalizeWords
import nl.chimpgamer.betterchestshops.paper.utils.LocationUtils
import nl.chimpgamer.betterchestshops.paper.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ChestShop(
    val id: Int,
    val creatorUUID: UUID,
    val containerType: ContainerType,
    val amount: Int,
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val itemStack: ItemStack?,
    val buyPrice: BigDecimal?,
    val sellPrice: BigDecimal?,
    val created: LocalDateTime
) {

    val signLocation = LocationUtils.buildBlockLocation(world, x, y, z)

    val isChunkLoaded: Boolean get() = signLocation.block.world.isChunkLoaded(signLocation.blockX shr 4, signLocation.blockZ shr 4)

    val isValid: Boolean get() = ChestShopSign.isValid(signLocation.block)

    val container: Container?
        get() {
            val signBlockState = signLocation.block.state
            if (signBlockState is Sign) {
                if (ChestShopSign.isValid(signBlockState)) {
                    return uBlock.findConnectedContainer(signBlockState)
                }
            }
            return null
        }

    val creatorName: String? = if (BetterChestShopsPlugin.instance.consoleUUID == creatorUUID) "Admin Shop" else Bukkit.getServer().getOfflinePlayer(creatorUUID).name

    val friendlyItemTypeName = (itemStack?.type?.name ?: "Nothing").lowercase().capitalizeWords()

    val buyPriceFormatted = Utils.formatPrice(buyPrice)
    val sellPriceFormatted = Utils.formatPrice(sellPrice)

    fun spawnItem() {
        val container = this.container ?: return

        val blockAboveContainer = container.block.getRelative(BlockFace.UP)

        val hologramHandler = BetterChestShopsPlugin.instance.hologramManager.hologramHandler
        hologramHandler.destroyItem(container.location)
        if (!blockAboveContainer.type.isAir) return
        val itemStack = this.itemStack ?: return
        if (itemStack.type.isAir || itemStack.amount < 1) return

        hologramHandler.displayItem(this, container.location, itemStack)
    }

    fun destroyItem() {
        val container = this.container ?: return
        BetterChestShopsPlugin.instance.hologramManager.hologramHandler.destroyItem(container.location)
    }
}