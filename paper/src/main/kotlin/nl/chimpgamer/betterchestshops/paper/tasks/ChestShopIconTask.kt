package nl.chimpgamer.betterchestshops.paper.tasks

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.CoroutineStart
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin

class ChestShopIconTask(private val plugin: BetterChestShopsPlugin) : Runnable {

    override fun run() {
        val time = System.currentTimeMillis()
        var count = 0
        plugin.chestShopsHandler.getChestShops().forEach { chestShop ->
            val signLocation = chestShop.signLocation
            // Check if chunk is loaded before continuing.
            if (!signLocation.isChunkLoaded) return@forEach
            count++
            plugin.launch(plugin.bootstrap.regionDispatcher(signLocation), CoroutineStart.UNDISPATCHED) {
                chestShop.spawnItem()
            }
        }

        plugin.debug { "ChestShopIconTask took ${System.currentTimeMillis() - time}ms to run through $count chestshops." }
    }
}