package nl.chimpgamer.betterchestshops.paper.models

import org.bukkit.Material

enum class ContainerType(val material: Material) {
    CHEST(Material.CHEST),
    TRAPPED_CHEST(Material.TRAPPED_CHEST),
    BARREL(Material.BARREL),
}
