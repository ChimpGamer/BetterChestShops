package nl.chimpgamer.betterchestshops.paper.holograms

import com.Zrips.CMI.CMI
import com.Zrips.CMI.Modules.Holograms.CMIHologram
import com.Zrips.CMI.Modules.Holograms.CMIHologramLineIcon
import com.Zrips.CMI.Modules.Holograms.CMIHologramType
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.holograms.HologramHandler.Companion.BARREL_HEIGHT_ADJUSTMENT
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CMIHologramHandler(private val plugin: BetterChestShopsPlugin) : HologramHandler {
    private val locationToHologram: MutableMap<Location, CMIHologram> = ConcurrentHashMap()

    override val name: String = "CMI"

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {
        var displayLocation = Location(
            containerLocation.world,
            containerLocation.x + plugin.settingsConfig.hologramOffSetX,
            containerLocation.y + plugin.settingsConfig.hologramOffSetY,
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )
        // Barrels are higher than chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, BARREL_HEIGHT_ADJUSTMENT, 0.0)

        val hologram = CMIHologram(UUID.randomUUID().toString(), displayLocation)
        hologram.type = CMIHologramType.ArmorStand

        val enchanted = itemStack.enchantments.isNotEmpty()

        val lineIcon = CMIHologramLineIcon("ICON:${itemStack.type}" + if (enchanted) "%enchanted%" else "")
        lineIcon.setIcon(itemStack)
        hologram.pages.addLine(lineIcon.iconText)
        hologram.settings.apply {
            visibilityRange = 20
            updateIntervalTicks = 0
            isSaveToFile = false
        }
        hologram.update()

        locationToHologram[containerLocation] = hologram
        CMI.getInstance().hologramManager.add(hologram)
    }

    override fun destroyItem(location: Location) {
        val hologram = locationToHologram.remove(location) ?: return
        CMI.getInstance().hologramManager.remove(hologram)
    }

    override fun destroyItems() {
        locationToHologram.keys.forEach { destroyItem(it) }
    }
}