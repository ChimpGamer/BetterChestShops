package nl.chimpgamer.betterchestshops.paper.listeners

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import world.bentobox.bentobox.api.events.island.IslandPreclearEvent

class BentoBoxListener(private val plugin: BetterChestShopsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onIslandDelete(event: IslandPreclearEvent) {
        val toRemove = plugin.chestShopsHandler.getChestShops { event.oldIsland.inIslandSpace(it.signLocation) }.toSet()

        val amount = plugin.chestShopsHandler.removeChestShops(toRemove)
        plugin.logger.info("Removed ${amount.get()} chestshops from island ${event.oldIsland.name}")
    }
}