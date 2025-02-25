package nl.chimpgamer.betterchestshops.paper.utils

import net.kyori.adventure.text.Component
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.capitalizeWords
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.math.BigDecimal
import java.text.DecimalFormat

object Utils {

    /**
     * Get the enchantment as a list of string
     *
     * @param item The item
     * @param withLevel Should we also return the level?
     * @return The enchantment name
     */
    fun getEnchantAsString(item: ItemStack, withLevel: Boolean): List<String> {
        val enchantsStringList: MutableList<String> = ArrayList()
        var enchants = item.enchantments
        if (item.type == Material.ENCHANTED_BOOK) {
            val storageMeta = item.itemMeta as EnchantmentStorageMeta
            enchants = storageMeta.storedEnchants
        }
        for ((enchantment, value) in enchants) {
            var enchant = enchantment.key.key.capitalizeWords()
            if (withLevel) enchant += "|" + value as Int
            enchantsStringList.add(enchant)
        }
        return enchantsStringList
    }

    fun formatPrice(price: BigDecimal?): String? {
        if (price == null) return null
        val decimalFormat = DecimalFormat.getIntegerInstance()
        decimalFormat.maximumFractionDigits = 2
        decimalFormat.isGroupingUsed = false
        return decimalFormat.format(price)
    }

    fun createChatInputBuilderBase(
        plugin: BetterChestShopsPlugin,
        player: Player
    ): PlayerChatInput.PlayerChatInputBuilder<String> =
        PlayerChatInput.PlayerChatInputBuilder<String>(plugin.bootstrap, player)
            .onExpire {
                it.clearTitle()
                it.sendActionBar(Component.empty())
            }
            .onCancel {
                it.clearTitle()
                it.sendActionBar(Component.empty())
            }
            .setValue { _, s -> s }
            .expiresAfter(6000)
            .invalidInputMessage(null)
            .sendValueMessage(null)
            .onExpireMessage(null)
}