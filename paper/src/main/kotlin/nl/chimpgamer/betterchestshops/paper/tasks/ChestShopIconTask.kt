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

        measureTimeMillis {
            val chestShopsByChunk = unorderedChestShops
                .filter { it.isChunkLoaded }
                .groupBy { it.signLocation.chunk }

            chestShopsByChunk.forEach { (chunk, chestShops) ->
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