package nl.chimpgamer.betterchestshops.paper.tasks

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.withContext
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import kotlin.system.measureTimeMillis

class ChestShopIconTask(private val plugin: BetterChestShopsPlugin) {

    suspend fun run() {
        var count = 0
        val unorderedChestShops = plugin.chestShopsHandler.getChestShopsUnordered()
        /*measureTimeMillis {
            chestShops = plugin.chestShopsHandler.getChunkWithChestShops()
        }.also { plugin.debug { "It took ${it}ms to build Map With Chunk and ChestShops" } }*/

        measureTimeMillis {
            val chestShopsByChunk = unorderedChestShops
                .filter { it.isChunkLoaded }
                .groupBy { it.signLocation.chunk }

            chestShopsByChunk.forEach { (chunk, chestShops) ->
                /*plugin.server.scheduler.runTask(plugin.bootstrap, Runnable {
                        chestShop.spawnItem()
                    })*/
                /*plugin.server.regionScheduler.execute(plugin.bootstrap, chunk.world, chunk.x, chunk.z) {
                    count++
                    chestShop.forEach(ChestShop::spawnItem)
                }*/
                withContext(plugin.bootstrap.regionDispatcher(chunk.world, chunk.x, chunk.z)) {
                    count += chestShops.size
                    chestShops.forEach(ChestShop::spawnItem)
                }
            }
        }.also {
            plugin.debug { "ChestShopIconTask took ${it}ms to spawn item for $count/${unorderedChestShops.size} chestshops." }
        }
    }
}