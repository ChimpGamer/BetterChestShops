package nl.chimpgamer.betterchestshops.paper.holograms

import com.Zrips.CMI.CMI
import com.Zrips.CMI.Modules.Holograms.CMIHologram
import net.Zrips.CMILib.Items.CMIItemStack
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
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
            containerLocation.y + plugin.settingsConfig.hologramOffSetY + 0.5, // Add extra 0.5 because that is CMI default icon spacing
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )
        // Barrels are higher then chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, 0.15, 0.0)

        val hologram = CMIHologram(UUID.randomUUID().toString(), displayLocation)

        val enchanted = itemStack.enchantments.isNotEmpty()

        hologram.addLine("ICON:${itemStack.type}" + if (enchanted) "%enchanted%" else "")
        hologram.updateIntervalSec = -1.0 // Disable updating of the hologram
        hologram.showRange = 20
        hologram.update()

        locationToHologram[containerLocation] = hologram
        CMI.getInstance().hologramManager.addHologram(hologram)

        val page = hologram.getPage(1) ?: return
        val line = page.lines[0] ?: return
        line.item = CMIItemStack(itemStack)
    }

    override fun destroyItem(location: Location) {
        val hologram = locationToHologram.remove(location) ?: return
        CMI.getInstance().hologramManager.removeHolo(hologram)
    }

    override fun destroyItems() {
        locationToHologram.keys.forEach { destroyItem(it) }
    }
}