package nl.chimpgamer.betterchestshops.paper.managers

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.holograms.*

class HologramManager(private val plugin: BetterChestShopsPlugin) {
    var hologramHandler: HologramHandler = EmptyHologramHandler()

    fun initialize() {
        determineHologramHandler()

        plugin.logger.info(hologramHandler::class.java.simpleName + " is the hologramHandler")
    }

    fun reload() {
        hologramHandler.destroyItems()

        initialize()
        plugin.chestShopIconTask.run()
    }

    private fun determineHologramHandler() {
        val hologramHandlerSetting = plugin.settingsConfig.hologramHandler
        if (hologramHandlerSetting == "auto") {
            when {
                isPluginEnabled("DecentHolograms") -> hologramHandler = DecentHologramsHologramHandler(plugin)
                isPluginEnabled("CMI") -> hologramHandler = CMIHologramHandler(plugin)
                isPluginEnabled("HolographicDisplays") -> hologramHandler = HolographicDisplaysHologramHandler(plugin)
            }
        } else {
            when {
                hologramHandlerSetting.equals("DecentHolograms", ignoreCase = true) && isPluginEnabled("DecentHolograms") -> {
                    hologramHandler = DecentHologramsHologramHandler(plugin)
                }
                hologramHandlerSetting.equals("CMI", ignoreCase = true) && isPluginEnabled("CMI") -> {
                    hologramHandler = CMIHologramHandler(plugin)
                }
                hologramHandlerSetting.equals("HolographicDisplays", ignoreCase = true) && isPluginEnabled("CMI") -> {
                    hologramHandler = HolographicDisplaysHologramHandler(plugin)
                }
            }
        }
    }

    private fun isPluginEnabled(pluginName: String) = plugin.server.pluginManager.isPluginEnabled(pluginName)
}