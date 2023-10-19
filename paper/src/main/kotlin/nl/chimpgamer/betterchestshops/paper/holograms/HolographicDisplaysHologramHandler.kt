package nl.chimpgamer.betterchestshops.paper.holograms

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI
import me.filoghost.holographicdisplays.api.hologram.Hologram
import me.filoghost.holographicdisplays.api.hologram.PlaceholderSetting
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class HolographicDisplaysHologramHandler(private val plugin: BetterChestShopsPlugin) : HologramHandler {
    private val locationToHologram: MutableMap<Location, Hologram> = ConcurrentHashMap()

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {
        var displayLocation = Location(
            containerLocation.world,
            containerLocation.x + plugin.settingsConfig.hologramOffSetX,
            containerLocation.y + plugin.settingsConfig.hologramOffSetY + 0.6,
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )
        // Barrels are higher then chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, 0.15, 0.0)

        val api = HolographicDisplaysAPI.get(plugin.bootstrap)
        val hologram = api.createHologram(displayLocation)
        hologram.lines.insertItem(0, itemStack)
        hologram.placeholderSetting = PlaceholderSetting.DISABLE_ALL
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