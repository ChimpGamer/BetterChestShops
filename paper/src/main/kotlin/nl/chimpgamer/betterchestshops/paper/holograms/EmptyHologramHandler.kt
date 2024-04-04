package nl.chimpgamer.betterchestshops.paper.holograms

import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class EmptyHologramHandler : HologramHandler {

    override val name: String = "empty"

    override fun displayItem(chestShop: ChestShop, containerLocation: Location, itemStack: ItemStack) {

    }

    override fun destroyItem(location: Location) {

    }

    override fun destroyItems() {

    }
}