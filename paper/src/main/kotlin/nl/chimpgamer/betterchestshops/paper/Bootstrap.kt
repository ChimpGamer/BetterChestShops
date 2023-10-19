package nl.chimpgamer.betterchestshops.paper

import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import org.bukkit.plugin.java.JavaPlugin

class Bootstrap : JavaPlugin() {
    private var plugin: BetterChestShopsPlugin? = null

    var inventoryManager = InventoryManager(this)
    override fun onLoad() {
        plugin = BetterChestShopsPlugin(this)
        plugin?.load()
    }

    override fun onEnable() {
        inventoryManager.invoke()
        plugin?.enable()
    }

    override fun onDisable() {
        plugin?.disable()
    }
}