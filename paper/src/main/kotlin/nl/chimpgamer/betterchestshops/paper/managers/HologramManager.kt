package nl.chimpgamer.betterchestshops.paper.managers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import kotlinx.coroutines.CoroutineStart
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.holograms.*

class HologramManager(private val plugin: BetterChestShopsPlugin) {
    var hologramHandler: HologramHandler = EmptyHologramHandler()

    /**
     * DecentHolograms is thread-safe
     * CMI is not thread-safe
     * HolographicDisplays is untested for thread-safety
     * FancyHolograms is not thread-safe
     */

    fun initialize() {
        determineHologramHandler()

        plugin.logger.info(hologramHandler.name + " is the hologramHandler")
    }

    fun reload() {
        hologramHandler.destroyItems()

        initialize()
        plugin.launch(plugin.bootstrap.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            plugin.chestShopIconTask.run()
        }
    }

    private fun determineHologramHandler() {
        val hologramHandlerSetting = plugin.settingsConfig.hologramHandler
        if (hologramHandlerSetting == "auto") {
            when {
                isPluginEnabled("DecentHolograms") -> hologramHandler = DecentHologramsHologramHandler(plugin)
                isPluginEnabled("CMI") -> hologramHandler = CMIHologramHandler(plugin)
                isPluginEnabled("HolographicDisplays") -> hologramHandler = HolographicDisplaysHologramHandler(plugin)
                isPluginEnabled("FancyHolograms") -> hologramHandler = FancyHologramsHologramHandler(plugin)
            }
        } else {
            when {
                hologramHandlerSetting.equals("DecentHolograms", ignoreCase = true) && isPluginEnabled("DecentHolograms") -> {
                    hologramHandler = DecentHologramsHologramHandler(plugin)
                }
                hologramHandlerSetting.equals("CMI", ignoreCase = true) && isPluginEnabled("CMI") -> {
                    hologramHandler = CMIHologramHandler(plugin)
                }
                hologramHandlerSetting.equals("HolographicDisplays", ignoreCase = true) && isPluginEnabled("HolographicDisplays") -> {
                    hologramHandler = HolographicDisplaysHologramHandler(plugin)
                }
                hologramHandlerSetting.equals("FancyHolograms", ignoreCase = true) && isPluginEnabled("FancyHolograms") -> {
                    hologramHandler = FancyHologramsHologramHandler(plugin)
                }
            }
        }
    }

    private fun isPluginEnabled(pluginName: String) = plugin.server.pluginManager.isPluginEnabled(pluginName)
}