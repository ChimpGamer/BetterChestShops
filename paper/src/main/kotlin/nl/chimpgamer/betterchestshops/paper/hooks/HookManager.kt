package nl.chimpgamer.betterchestshops.paper.hooks

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class HookManager(plugin: BetterChestShopsPlugin) : Listener {
    private val placeholderAPIHook = PlaceholderAPIHook(plugin)

    fun load() {
        placeholderAPIHook.load()
    }

    fun unload() {
        placeholderAPIHook.unload()
    }

    @EventHandler
    fun PluginEnableEvent.onPluginEnable() {
        when (plugin.name) {
            "PlaceholderAPI" -> placeholderAPIHook.load()
        }
    }

    @EventHandler
    fun PluginDisableEvent.onPluginDisable() {
        when (plugin.name) {
            "PlaceholderAPI" -> placeholderAPIHook.unload()
        }
    }
}