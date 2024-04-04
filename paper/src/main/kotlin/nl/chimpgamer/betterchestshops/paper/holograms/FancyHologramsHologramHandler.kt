package nl.chimpgamer.betterchestshops.paper.holograms

import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.Hologram
import de.oliver.fancyholograms.api.HologramType
import de.oliver.fancyholograms.api.data.DisplayHologramData
import de.oliver.fancyholograms.api.data.HologramData
import de.oliver.fancyholograms.api.data.ItemHologramData
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.Location
import org.bukkit.block.data.Directional
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FancyHologramsHologramHandler(private val plugin: BetterChestShopsPlugin) : HologramHandler {
    private val locationToHologram: MutableMap<Location, Hologram> = ConcurrentHashMap()

    override val name: String = "FancyHolograms"

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {
        var displayLocation = Location(
            containerLocation.world,
            containerLocation.x + plugin.settingsConfig.hologramOffSetX,
            containerLocation.y + plugin.settingsConfig.hologramOffSetY + 0.6,
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )

        val signBlockData = chestShop.signLocation.block.blockData
        if (signBlockData is Directional) {
            displayLocation.direction = signBlockData.facing.direction
        }

        // Barrels are higher then chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, 0.15, 0.0)

        val hologramManager = FancyHologramsPlugin.get().hologramManager
        val displayData = DisplayHologramData.getDefault(displayLocation)
        displayData.billboard = Display.Billboard.FIXED
        val itemHologramData = ItemHologramData.getDefault().setItem(itemStack)
        val hologramData = HologramData(UUID.randomUUID().toString(), displayData, HologramType.ITEM, itemHologramData)
        val hologram = hologramManager.create(hologramData)

        hologram.createHologram()
        hologram.showHologram(plugin.server.onlinePlayers)
        locationToHologram[containerLocation] = hologram
    }

    override fun destroyItem(location: Location) {
        val hologram = locationToHologram.remove(location) ?: return
        plugin.runSync { hologram.hideHologram(plugin.server.onlinePlayers) }
        hologram.deleteHologram()
    }

    override fun destroyItems() {
        locationToHologram.keys.forEach { destroyItem(it) }
    }
}