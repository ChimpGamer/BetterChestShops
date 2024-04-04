package nl.chimpgamer.betterchestshops.paper.holograms

import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

interface HologramHandler {
    val name: String

    fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack)

    fun destroyItem(location: Location)

    fun destroyItems()
}