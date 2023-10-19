package nl.chimpgamer.betterchestshops.paper.storage.tables

import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ChestShopsTable: IntIdTable("chest_shops") {
    val creatorUUID = uuid("creator_uuid")
    val containerType = enumerationByName("container_type", 20, ContainerType::class)
    val amount = integer("amount")
    val world = varchar("world", 50)
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val itemStack = blob("item_stack").nullable()
    val buyPrice = decimal("buy_price",19,4).nullable()
    val sellPrice = decimal("sell_price",19,4).nullable()
    val created = datetime("created")
}