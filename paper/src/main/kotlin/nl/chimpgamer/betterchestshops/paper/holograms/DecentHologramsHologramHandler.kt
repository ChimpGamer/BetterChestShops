package nl.chimpgamer.betterchestshops.paper.holograms

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DecentHologramsHologramHandler(private val plugin: BetterChestShopsPlugin) : HologramHandler {
    private val locationToHologram: MutableMap<Location, Hologram> = ConcurrentHashMap()

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {
        var displayLocation = Location(
            containerLocation.world,
            containerLocation.x + plugin.settingsConfig.hologramOffSetX,
            containerLocation.y + plugin.settingsConfig.hologramOffSetY + 0.6, // Add extra 0.6 because that is DecentHolograms default icon spacing
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )
        // Barrels are higher then chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, 0.15, 0.0)

        val hologram = DHAPI.createHologram(UUID.randomUUID().toString(), displayLocation)
        DHAPI.addHologramLine(hologram, itemStack)
        locationToHologram[containerLocation] = hologram
    }

    override fun destroyItem(location: Location) {
        val hologram = locationToHologram.remove(location) ?: return
        hologram.delete()
    }

    override fun destroyItems() {
        locationToHologram.keys.forEach { destroyItem(it) }
    }
}