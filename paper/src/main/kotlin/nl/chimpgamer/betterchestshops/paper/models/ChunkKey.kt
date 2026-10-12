package nl.chimpgamer.betterchestshops.paper.models

import java.util.UUID

data class ChunkKey(
    val worldId: UUID,
    val x: Int,
    val z: Int
)