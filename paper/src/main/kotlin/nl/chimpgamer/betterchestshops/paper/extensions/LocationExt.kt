package nl.chimpgamer.betterchestshops.paper.extensions

import nl.chimpgamer.betterchestshops.paper.models.ChunkKey
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.block.BlockFace

fun Location.toFormattedString() = "${world.name}:$x:$y:$z"

fun Location.isSafe(): Boolean {
    try {
        val blockAtFeet = block
        if (blockAtFeet.type.let { !Tag.SIGNS.isTagged(it) && it.isOccluding }
            && blockAtFeet.location.add(0.0, 1.0,0.0).block.type.let { !Tag.SIGNS.isTagged(it) && it.isOccluding}) {
            return false
        }
        val blockAtHead = blockAtFeet.getRelative(BlockFace.UP)
        if (!Tag.SIGNS.isTagged(blockAtHead.type) && blockAtHead.type.isOccluding) {
            return false
        }
        val blockOnGround = blockAtFeet.getRelative(BlockFace.DOWN)
        return blockOnGround.type.isSolid
    } catch (ex: Exception) {
        return false
    }
}

fun Location.toChunkKey(): ChunkKey {
    return ChunkKey(world.uid, blockX shr 4, blockZ shr 4)
}

fun Chunk.toChunkKey(): ChunkKey = ChunkKey(world.uid, x, z)