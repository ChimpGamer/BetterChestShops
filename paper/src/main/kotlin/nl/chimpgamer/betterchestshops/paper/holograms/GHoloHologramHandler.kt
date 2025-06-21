package nl.chimpgamer.betterchestshops.paper.holograms

import dev.geco.gholo.api.GHoloAPI
import dev.geco.gholo.`object`.holo.GHolo
import dev.geco.gholo.`object`.holo.GHoloRow
import dev.geco.gholo.`object`.simple.SimpleLocation
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.holograms.HologramHandler.Companion.BARREL_HEIGHT_ADJUSTMENT
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GHoloHologramHandler(private val plugin: BetterChestShopsPlugin) : HologramHandler {
    private val locationToHologram: MutableMap<Location, GHolo> = ConcurrentHashMap()

    override val name: String = "GHolo"

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {
        var displayLocation = Location(
            containerLocation.world,
            containerLocation.x + plugin.settingsConfig.hologramOffSetX,
            containerLocation.y + plugin.settingsConfig.hologramOffSetY,
            containerLocation.z + plugin.settingsConfig.hologramOffSetZ
        )
        // Barrels are higher than chests
        if (chestShop.containerType !== ContainerType.BARREL) displayLocation = displayLocation.subtract(0.0, BARREL_HEIGHT_ADJUSTMENT, 0.0)
        val hologram = createHologram(displayLocation, itemStack)
        locationToHologram[containerLocation] = hologram
    }

    override fun destroyItem(location: Location) {
        val hologram = locationToHologram.remove(location) ?: return
        GHoloAPI.getInstance().holoService.removeHolo(hologram)
    }

    override fun destroyItems() {
        locationToHologram.keys.forEach { destroyItem(it) }
    }

    private fun createHologram(displayLocation: Location, itemStack: ItemStack): GHolo {
        val holoService = GHoloAPI.getInstance().holoService
        val hologram = GHolo(
            UUID.randomUUID(),
            UUID.randomUUID().toString(),
            SimpleLocation.fromBukkitLocation(displayLocation)
        )

        val gHoloMain = GHoloAPI.getInstance()
        holoService.holos.add(hologram)
        val content = gHoloMain.textFormatUtil.replaceSymbols("itemstack:${itemStack.type.name}")
        val holoRow= GHoloRow(hologram, content)
        hologram.addRow(holoRow)
        gHoloMain.entityUtil.createHoloRowEntity(holoRow)

        return hologram
    }
}