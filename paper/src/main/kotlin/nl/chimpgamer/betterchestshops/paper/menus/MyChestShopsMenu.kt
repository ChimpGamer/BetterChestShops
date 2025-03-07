package nl.chimpgamer.betterchestshops.paper.menus

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.events.RyseInventoryOpenEvent
import io.github.rysefoxx.inventory.plugin.other.EventCreator
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.*
import nl.chimpgamer.betterchestshops.paper.utils.Constants
import nl.chimpgamer.betterchestshops.paper.utils.Utils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.time.Duration

class MyChestShopsMenu(private val plugin: BetterChestShopsPlugin) : InventoryProvider {

    val inventory = RyseInventory.builder()
        .listener(EventCreator(RyseInventoryOpenEvent::class.java) {
            plugin.inventoryManager.getContents(it.player.uniqueId).ifPresent { contents ->
                val pagination = contents.pagination()
                val contentPlaceholders = mapOf(
                    "page" to pagination.page(),
                    "maxpage" to pagination.lastPage()
                )
                contents.updateTitle("<red>My ChestShops <yellow>(<page>/<maxpage>)".parse(contentPlaceholders))
            }
        })
        .size(54)
        .title("<red>My ChestShops".parse())
        .provider(this)
        .build(plugin.bootstrap)

    val loreHeader = "<white>Lore:".parse()
    val enchantsHeader = "<white>Enchant(s):".parse()
    val none = listOf("<gray>(None)".parse())
    val clickToTeleport = listOf(Component.empty(), "<light_purple>Click to teleport".parse())

    override fun init(player: Player, contents: InventoryContents) {
        var chestShops = plugin.chestShopsHandler.getAllByCreator(player.uniqueId).reversed()

        val pagination = contents.pagination().apply {
            itemsPerPage = 45
            iterator(SlotIterator.builder().startPosition(0).type(SlotIterator.SlotIteratorType.HORIZONTAL).build())
        }

        val searchInput = contents.getProperty<String>("mychestshop_search_input")
        if (searchInput != null) {
            chestShops = chestShops.filter {
                it.itemStack?.type?.name?.contains(searchInput, ignoreCase = true) == true ||
                        it.containerType.name.contains(searchInput, ignoreCase = true)
            }
        }

        val hasTeleportPermission = player.hasPermission("betterchestshops.teleport")

        chestShops.forEach { chestShop ->
            with(chestShop) {
                if (this.itemStack == null) return@forEach

                val tagResolver = TagResolver.resolver(
                    Placeholder.component("item_display_name", itemStack.displayName()),
                    Placeholder.unparsed("item_friendly_type", friendlyItemTypeName),
                )

                val loreLine1 = if (buyPriceFormatted != null && sellPriceFormatted != null) {
                    "<white>Buy/Sell Price: <yellow>$buyPriceFormatted/$sellPriceFormatted Coin(s)"
                } else if (sellPriceFormatted != null) {
                    "<white>Sell Price: <yellow>$sellPriceFormatted Coin(s)"
                } else if (buyPriceFormatted != null) {
                    "<white>Buy Price: <yellow>$buyPriceFormatted Coin(s)"
                } else {
                    "<red>Invalid ChestShop!"
                }

                val chestShopItemLore = itemStack.lore() ?: none

                val chestShopItemEnchants = Utils.getEnchantAsString(itemStack, withLevel = true)
                val chestShopItemEnchantsComponent = if (chestShopItemEnchants.isNotEmpty()) {
                    chestShopItemEnchants.map { enchant ->
                        val ecData = enchant.split("|")
                        "   <yellow>- ${ecData[0]} <gold>[${ecData[1]}]".parse()
                    }
                } else {
                    none
                }

                val mutableLore = mutableListOf(
                    loreLine1.parse(),
                    "<white>Item Type: <yellow><item_friendly_type> <gray>(x$amount)".parse(tagResolver),
                    "<white>Display Name: <yellow><item_display_name>".parse(tagResolver),
                    loreHeader
                ).apply {
                    addAll(chestShopItemLore)
                    add(enchantsHeader)
                    addAll(chestShopItemEnchantsComponent)
                    if (hasTeleportPermission) {
                        addAll(clickToTeleport)
                    }
                }

                // Setting the lore takes the most time in this process
                val item = ItemStack(containerType.material)
                    .meta {
                        displayName(signLocation.toFormattedString().parse())
                        lore(mutableLore)
                    }

                pagination.addItem(IntelligentItem.of(item) {
                    if (hasTeleportPermission) {
                        var location = chestShop.signLocation
                        if (!location.isSafe()) {
                            val l = chestShop.signLocation.clone().subtract(0.0, 1.0, 0.0)
                            if (l.isSafe()) {
                                location = l
                            } else {
                                player.sendRichMessage("<red>Teleport location is unsafe!")
                                return@of
                            }
                        }
                        player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    }
                })
            }
        }

        contents[45] = IntelligentItem.of(Constants.previousPageButton.richName("<yellow>⇽ <gold>Previous Page")) {
            if (!pagination.isFirst) {
                inventory.open(player, pagination.previous().page())
            }
        }

        contents[49] = IntelligentItem.of(ItemStack(Material.IRON_DOOR).richName("<red>Close")) {
            inventory.close(player)
        }

        contents[51] = IntelligentItem.of(ItemStack(Material.COMPASS).richName("<gold>Search ChestShop")) {
            inventory.close(player)
            val playerChatInputBuilder = Utils.createChatInputBuilderBase(plugin, player)
                .isValidInput { _, input -> input.isNotEmpty() }
                .onFinish { player, input ->
                    player.clearTitle()
                    inventory.open(player, mapOf("mychestshop_search_input" to input))
                }
            val playerChatInput = playerChatInputBuilder.build()
            playerChatInput.start()
            val title = Title.title(
                "<red><bold>Search ChestShop".parse(), "<red>Search by item or container type!".parse(),
                Title.Times.times(Ticks.duration(10), Duration.ofSeconds(6000), Ticks.duration(20))
            )
            player.showTitle(title)
        }

        contents[53] = IntelligentItem.of(Constants.nextPageButton.richName("<gold>Next Page <yellow>⇾")) {
            if (!pagination.isLast) {
                inventory.open(player, pagination.next().page())
            }
        }
    }
}