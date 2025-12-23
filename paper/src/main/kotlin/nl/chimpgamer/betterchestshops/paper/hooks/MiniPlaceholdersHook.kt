package nl.chimpgamer.betterchestshops.paper.hooks

import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.toComponent
import org.bukkit.entity.Player

class MiniPlaceholdersHook internal constructor(plugin: BetterChestShopsPlugin) : PluginHook(plugin, "MiniPlaceholders") {
    lateinit var expansion: Expansion

    override fun load() {
        if (!canHook()) return
        val builder = Expansion.builder("betterchestshops")

            .audiencePlaceholder(Player::class.java,"chestshops_created") { audience, _, _ ->
                selfClosingInserting(plugin.chestShopsHandler.getAllByCreator(audience.uniqueId).size.toComponent())
            }
            .audiencePlaceholder(Player::class.java,"chestshop_limit") { audience, _, _ ->
                selfClosingInserting(plugin.getChestShopLimit(audience).toComponent())
            }
            .audiencePlaceholder(Player::class.java,"chestshop_has_reached_limit") { audience, _, _ ->
                selfClosingInserting(plugin.hasReachedLimit(audience).toComponent())
            }

        expansion = builder.build()
            .also { it.register() }
        isLoaded = true
    }

    override fun unload() {
        if (this::expansion.isInitialized && expansion.registered()) {
            expansion.unregister()
        }
    }

}