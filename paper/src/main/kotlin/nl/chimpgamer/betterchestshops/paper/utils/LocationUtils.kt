package nl.chimpgamer.betterchestshops.paper.utils

import org.bukkit.Bukkit
import org.bukkit.Location

object LocationUtils {

    fun buildBlockLocation(worldName: String, x: Int, y: Int, z: Int): Location {
        val world = Bukkit.getWorld(worldName) ?: throw IllegalArgumentException("unknown world")
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}