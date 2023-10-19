package nl.chimpgamer.betterchestshops.paper.storage.entities

import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.storage.tables.ChestShopsTable
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ChestShopEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChestShopEntity>(ChestShopsTable)

    var creatorUUID by ChestShopsTable.creatorUUID
    var containerType by ChestShopsTable.containerType
    var amount  by ChestShopsTable.amount
    var world by ChestShopsTable.world
    var x by ChestShopsTable.x
    var y by ChestShopsTable.y
    var z by ChestShopsTable.z
    var itemStack by ChestShopsTable.itemStack
    var buyPrice by ChestShopsTable.buyPrice
    var sellPrice by ChestShopsTable.sellPrice
    var created by ChestShopsTable.created


}

fun ChestShopEntity.toChestShop() = ChestShop(id.value, creatorUUID, containerType, amount, world, x,y,z,
    if (itemStack == null) null else ItemStack.deserializeBytes(itemStack!!.bytes), buyPrice, sellPrice, created)