package nl.chimpgamer.betterchestshops.paper.tasks

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.withContext
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import kotlin.system.measureTimeMillis

class ChestShopIconTask(private val plugin: BetterChestShopsPlugin) {

    suspend fun run() {
        var count = 0

        val chestShops = plugin.chestShopsHandler.getChestShopsUnordered()
        /*measureTimeMillis {
            chestShops = plugin.chestShopsHandler.getChunkWithChestShops()
        }.also { plugin.debug { "It took ${it}ms to build Map With Chunk and ChestShops" } }*/

        measureTimeMillis {
            chestShops.forEach { chestShop ->
                val signLocation = chestShop.signLocation
                // Check if chunk is loaded before continuing.
                if (!signLocation.isChunkLoaded) return@forEach
                count++
                withContext(plugin.bootstrap.regionDispatcher(signLocation)) {
                    chestShop.spawnItem()
                }
            }


            /*chestShops.forEach { (chunk, chestShops) ->
                if (!chunk.isLoaded) return@forEach
                chunk.addPluginChunkTicket(plugin.bootstrap)
                withContext(plugin.bootstrap.regionDispatcher(chunk.world, chunk.x, chunk.z)) {
                    chestShops.forEach { chestShop ->
                        count++
                        chestShop.spawnItem()
                    }
                }
                chunk.removePluginChunkTicket(plugin.bootstrap)
            }*/
        }.also {
            plugin.debug { "ChestShopIconTask took ${it}ms to spawn item for $count/${chestShops.size} chestshops." }
        }
    }
}