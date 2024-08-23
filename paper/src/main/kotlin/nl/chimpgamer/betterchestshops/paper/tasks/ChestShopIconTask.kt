package nl.chimpgamer.betterchestshops.paper.tasks

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.withContext
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin

class ChestShopIconTask(private val plugin: BetterChestShopsPlugin) {

    suspend fun run() {
        val time = System.currentTimeMillis()
        var count = 0
        plugin.chestShopsHandler.getChestShops().forEach { chestShop ->
            val signLocation = chestShop.signLocation
            // Check if chunk is loaded before continuing.
            if (!signLocation.isChunkLoaded) return@forEach
            count++
            withContext(plugin.bootstrap.regionDispatcher(signLocation)) {
                chestShop.spawnItem()
            }
        }

        plugin.debug { "ChestShopIconTask took ${System.currentTimeMillis() - time}ms to run through $count chestshops." }
    }
}