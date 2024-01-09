package nl.chimpgamer.betterchestshops.paper.tasks

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
            plugin.runSync { chestShop.spawnItem() }
        }

        val end = System.currentTimeMillis() - time
        plugin.debug("ChestShopIconTask took ${end}ms to run through $count chestshops.")
    }
}