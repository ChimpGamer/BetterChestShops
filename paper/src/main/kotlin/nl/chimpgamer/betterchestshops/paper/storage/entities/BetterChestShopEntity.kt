package nl.chimpgamer.betterchestshops.paper.storage.entities

import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.storage.tables.BetterChestShopsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class BetterChestShopEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BetterChestShopEntity>(BetterChestShopsTable)

    var creatorUUID by BetterChestShopsTable.creatorUUID
    var containerType by BetterChestShopsTable.containerType
    var amount  by BetterChestShopsTable.amount
    var signLocation by BetterChestShopsTable.signLocation
    var itemStack by BetterChestShopsTable.itemStack
    var buyPrice by BetterChestShopsTable.buyPrice
    var sellPrice by BetterChestShopsTable.sellPrice
    var created by BetterChestShopsTable.created
}

fun BetterChestShopEntity.toChestShop() = ChestShop(id.value, creatorUUID, containerType, amount, signLocation, itemStack, buyPrice, sellPrice, created)