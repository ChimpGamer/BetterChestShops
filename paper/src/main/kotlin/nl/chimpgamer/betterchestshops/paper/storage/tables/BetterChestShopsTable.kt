package nl.chimpgamer.betterchestshops.paper.storage.tables

import nl.chimpgamer.betterchestshops.paper.models.ContainerType
import nl.chimpgamer.betterchestshops.paper.utils.BlockLocationSerializer
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.vendors.currentDialect

object BetterChestShopsTable: IntIdTable("better_chest_shops") {
    val creatorUUID = uuid("creator_uuid")
    val containerType = enumerationByName("container_type", 20, ContainerType::class)
    val amount = integer("amount")
    val signLocation: Column<Location> = registerColumn("sign_location", BlockLocationColumnType)
    val itemStack: Column<ItemStack?> = registerColumn("item_stack", ItemStackColumnType).nullable()
    val buyPrice = decimal("buy_price",19,4).nullable()
    val sellPrice = decimal("sell_price",19,4).nullable()
    val created = datetime("created")
}

object BlockLocationColumnType : ColumnType<Location>() {

    override fun sqlType(): String = currentDialect.dataTypeProvider.binaryType()

    override fun valueFromDB(value: Any): Location? {
        // For some reason the value can also be just the location???
        return when (value) {
            is ByteArray -> BlockLocationSerializer.deserializeFromBytes(value)
            is Location -> value
            else -> null
        }
    }

    override fun valueToDB(value: Location?): Any? {
        if (value == null) return null
        return BlockLocationSerializer.serializeToBytes(value)
    }
}

object ItemStackColumnType : ColumnType<ItemStack>() {

    override fun sqlType(): String = currentDialect.dataTypeProvider.binaryType()

    override fun valueFromDB(value: Any): ItemStack? {
        // For some reason the value can also be just the ItemStack???
        return when (value) {
            is ByteArray -> ItemStack.deserializeBytes(value)
            is ItemStack -> value
            else -> null
        }
    }

    override fun valueToDB(value: ItemStack?): Any? {
        return value?.serializeAsBytes()
    }
}