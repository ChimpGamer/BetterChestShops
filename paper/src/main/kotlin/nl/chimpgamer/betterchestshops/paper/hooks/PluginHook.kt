package nl.chimpgamer.betterchestshops.paper.hooks

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin

abstract class PluginHook(protected val plugin: BetterChestShopsPlugin, val pluginName: String) {
    open var isLoaded: Boolean = false
    abstract fun load()
    open fun unload() {}

    fun canHook(): Boolean {
        return plugin.server.pluginManager.isPluginEnabled(pluginName)
    }

    fun isPluginLoaded(): Boolean {
        return plugin.server.pluginManager.getPlugin(pluginName) != null
    }
}