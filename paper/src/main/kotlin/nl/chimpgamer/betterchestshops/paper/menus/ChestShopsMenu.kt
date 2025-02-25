package nl.chimpgamer.betterchestshops.paper.menus

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.events.RyseInventoryOpenEvent
import io.github.rysefoxx.inventory.plugin.other.EventCreator
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.*
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import nl.chimpgamer.betterchestshops.paper.models.ChestShopSortBy
import nl.chimpgamer.betterchestshops.paper.utils.Constants
import nl.chimpgamer.betterchestshops.paper.utils.Utils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

class ChestShopsMenu(private val plugin: BetterChestShopsPlugin) : InventoryProvider {

    val inventory = RyseInventory.builder()
        .listener(EventCreator(RyseInventoryOpenEvent::class.java) {
            plugin.inventoryManager.getContents(it.player.uniqueId).ifPresent { contents ->
                val pagination = contents.pagination()
                val contentPlaceholders = mapOf(
                    "page" to pagination.page(),
                    "maxpage" to pagination.lastPage()
                )
                contents.updateTitle("<red>ChestShops <yellow>(<page>/<maxpage>)".parse(contentPlaceholders))
            }
        })
        .size(54)
        .title("<red>ChestShops".parse())
        .provider(this)
        .build(plugin.bootstrap)

    val loreHeader = "<white>Lore:".parse()
    val enchantsHeader = "<white>Enchant(s):".parse()
    val none = listOf("<gray>(None)".parse())
    val clickToTeleport = listOf(Component.empty(), "<light_purple>Click to teleport".parse())

    override fun init(player: Player, contents: InventoryContents) {
        var chestShops = plugin.chestShopsHandler.getChestShops().reversed()

        val pagination = contents.pagination().apply {
            itemsPerPage = 45
            iterator(SlotIterator.builder().startPosition(0).type(SlotIterator.SlotIteratorType.HORIZONTAL).build())
        }

        val sortBy = contents.getProperty<ChestShopSortBy>("chestshop_sort_by", ChestShopSortBy.BUY_AND_SELL_PRICE)
        chestShops = when (sortBy) {
            ChestShopSortBy.BUY_AND_SELL_PRICE -> chestShops.sortedWith(compareBy<ChestShop, BigDecimal?>(nullsLast()) { it.buyPrice}.thenBy { it.sellPrice })
            ChestShopSortBy.BUY_PRICE -> chestShops.sortedWith(compareBy(nullsLast()) { it.buyPrice })
            ChestShopSortBy.SELL_PRICE -> chestShops.sortedWith(compareBy(nullsLast()) { it.sellPrice })
            ChestShopSortBy.CREATOR -> chestShops.sortedWith(compareBy(nullsLast()) { it.creatorName })
        }

        val searchInput = contents.getProperty<String>("chestshop_search_input")
        if (searchInput != null) {
            chestShops = chestShops.filter {
                it.itemStack?.type?.name?.contains(searchInput, ignoreCase = true) == true ||
                        it.containerType.name.contains(searchInput, ignoreCase = true) ||
                        it.creatorName?.contains(searchInput, ignoreCase = true) == true
            }
        }

        val hasTeleportPermission = player.hasPermission("betterchestshops.teleport")

        val lastItemOfPage = pagination.itemsPerPage * pagination.page()
        val firstItemOfPage = lastItemOfPage - pagination.itemsPerPage

        chestShops.forEachIndexed { index, chestShop ->
            with(chestShop) {
                if (this.itemStack == null) return@forEachIndexed
                val buildItem = index in firstItemOfPage..lastItemOfPage

                val item = ItemStack(containerType.material)

                val tagResolver = TagResolver.resolver(
                    Placeholder.component("item_display_name", itemStack.displayName()),
                    Placeholder.unparsed("item_friendly_type", friendlyItemTypeName),
                )

                if (buildItem) {
                    val loreLine1 = if (buyPriceFormatted != null && sellPriceFormatted != null) {
                        "<white>Buy/Sell Price: <yellow>$buyPriceFormatted/$sellPriceFormatted Coin(s)"
                    } else if (sellPriceFormatted != null) {
                        "<white>Sell Price: <yellow>$sellPriceFormatted Coin(s)"
                    } else if (buyPriceFormatted != null) {
                        "<white>Buy Price: <yellow>$buyPriceFormatted Coin(s)"
                    } else {
                        "<red>Invalid ChestShop!"
                    }
                    val loreLine2 = "<white>Owner: <yellow>${creatorName ?: creatorUUID}"

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
                        loreLine2.parse(),
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
                    item.meta {
                        displayName(signLocation.toFormattedString().toComponent())
                        lore(mutableLore)
                    }
                }

                pagination.addItem(IntelligentItem.of(item) {
                    if (hasTeleportPermission) {
                        var location = chestShop.signLocation
                        if (!location.isSafe()) {
                            val l = chestShop.signLocation.clone().subtract(0.0, 1.0, 0.0)
                            if (l.isSafe()) {
                                location = l
                            } else {
                                val tpAnywayClickEvent = ClickEvent.callback({
                                    val uuid = it[Identity.UUID].getOrNull() ?: return@callback
                                    plugin.server.getPlayer(uuid)
                                        ?.teleportAsync(chestShop.signLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
                                }, ClickCallback.Options.builder().lifetime(Duration.ofMinutes(1L)).build())
                                val teleportIsUnsafeMessage =
                                    "Teleport location is unsafe!".toComponent(NamedTextColor.RED).append(Component.space())
                                        .append(
                                            "[Click here to teleport anyway]".toComponent(NamedTextColor.DARK_RED)
                                                .clickEvent(tpAnywayClickEvent)
                                        )
                                player.sendMessage(teleportIsUnsafeMessage)
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

        contents[47] = IntelligentItem.of(
            ItemStack(Material.HOPPER).richName("<gold>Sort")
                .richLore("<gray>Click to sort the chestshops in a specific order")
                .richLore("<gray>Currently sorted by: <yellow>$sortBy")
        ) {
            contents.setProperty("chestshop_sort_by", sortBy.next())
            contents.reload()
            val contentPlaceholders = mapOf(
                "page" to pagination.page(),
                "maxpage" to pagination.lastPage()
            )
            contents.updateTitle("<red>ChestShops <yellow>(<page>/<maxpage>)".parse(contentPlaceholders))
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
                    inventory.open(player, mapOf("chestshop_search_input" to input))
                }
            val playerChatInput = playerChatInputBuilder.build()
            playerChatInput.start()
            val title = Title.title(
                "<red><bold>Search ChestShop".parse(), "<red>Search by player, item or container type!".parse(),
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