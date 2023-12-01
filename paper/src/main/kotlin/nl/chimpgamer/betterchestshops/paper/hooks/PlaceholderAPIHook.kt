package nl.chimpgamer.betterchestshops.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: BetterChestShopsPlugin) {
    private lateinit var placeholderExpansion: BetterChestShopsPlaceholderExpansion

    private val name = "PlaceholderAPI"
    private val isEnabled get() = plugin.server.pluginManager.isPluginEnabled(name)

    fun load() {
        if (isEnabled) {
            placeholderExpansion = BetterChestShopsPlaceholderExpansion(plugin).also { it.register() }
            plugin.logger.info("Successfully loaded $name hook!")
        }
    }

    fun unload() {
        if (this::placeholderExpansion.isInitialized) {
            placeholderExpansion.unregister()
        }
    }
}

class BetterChestShopsPlaceholderExpansion(private val plugin: BetterChestShopsPlugin) : PlaceholderExpansion() {

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null

        return when (params) {
            "chestshops_created" -> plugin.chestShopsHandler.getAllByCreator(player.uniqueId).size.toString()
            "chestshop_limit" -> plugin.getChestShopLimit(player).toString()
            "chestshop_has_reached_limit" -> plugin.hasReachedLimit(player).toString()
             else -> return null
        }
    }

    override fun getIdentifier(): String = "betterchestshops"

    override fun getAuthor(): String = plugin.authors.joinToString()

    override fun getVersion(): String = plugin.version

    override fun persist(): Boolean = true

}