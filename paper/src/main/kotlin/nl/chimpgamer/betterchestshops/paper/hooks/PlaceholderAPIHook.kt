package nl.chimpgamer.betterchestshops.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import org.bukkit.entity.Player

class PlaceholderAPIHook(plugin: BetterChestShopsPlugin) : PluginHook(plugin, "PlaceholderAPI") {
    private lateinit var placeholderExpansion: BetterChestShopsPlaceholderExpansion

    override fun load() {
        if (canHook()) {
            placeholderExpansion = BetterChestShopsPlaceholderExpansion(plugin).also { it.register() }
            plugin.logger.info("Successfully loaded $pluginName hook!")
        }
    }

    override fun unload() {
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