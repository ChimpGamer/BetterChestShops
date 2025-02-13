package nl.chimpgamer.betterchestshops.paper.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import java.nio.ByteBuffer
import java.util.UUID

object BlockLocationSerializer {

    fun serializeToBytes(location: Location): ByteArray {
        val bb = ByteBuffer.wrap(ByteArray(40))
        bb.putLong(location.world.uid.mostSignificantBits)
        bb.putLong(location.world.uid.leastSignificantBits)

        bb.putDouble(location.x)
        bb.putDouble(location.y)
        bb.putDouble(location.z)

        return bb.array()
    }

    fun deserializeFromBytes(bytes: ByteArray): Location {
        val bb = ByteBuffer.wrap(bytes)
        val world = Bukkit.getWorld(UUID(bb.getLong(), bb.getLong())) ?: throw IllegalArgumentException("unknown world")
        return Location(world, bb.getDouble(), bb.getDouble(), bb.getDouble())
    }
}