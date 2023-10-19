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

    override fun init(player: Player, contents: InventoryContents) {
        var chestShops = plugin.chestShopsHandler.getChestShops().reversed()

        val pagination = contents.pagination()

        val searchInput = contents.getProperty<String>("chestshop_search_input")
        if (searchInput != null) {
            chestShops = chestShops.filter {
                it.itemStack?.type?.name?.contains(searchInput, ignoreCase = true) == true ||
                        it.containerType.name.contains(searchInput, ignoreCase = true) ||
                        it.creatorName?.contains(searchInput, ignoreCase = true) == true
            }
        }

        val hasTeleportPermission = player.hasPermission("betterchestshops.teleport")

        val loreHeader = "<white>Lore:".parse()
        val enchantsHeader = "<white>Enchant(s):".parse()
        val none = listOf("<gray>(None)".parse())
        val clickToTeleport = listOf(Component.empty(), "<light_purple>Click to teleport".parse())

        chestShops.forEach { chestShop ->
            with(chestShop) {
                if (this.itemStack == null) return@forEach

                val icon = runCatching { Material.valueOf(containerType.name) }.getOrNull() ?: Material.BARRIER

                val loreLine1 = if (buyPrice != null && sellPrice != null) {
                    "<white>Buy/Sell Price: <yellow>${Utils.formatPrice(buyPrice)}/${Utils.formatPrice(sellPrice)} Coin(s)"
                } else if (buyPrice != null) {
                    "<white>Sell Price: <yellow>${Utils.formatPrice(buyPrice)} Coin(s)"
                } else if (sellPrice != null) {
                    "<white>Buy Price: <yellow>${Utils.formatPrice(sellPrice)} Coin(s)"
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
                    listOf("<gray>(None)".parse())
                }

                val mutableLore = mutableListOf(
                    loreLine1.parse(),
                    "<white>Item Type: <yellow>${friendlyItemTypeName} <gray>(x$amount)".parse(),
                    loreHeader
                )
                mutableLore.apply {
                    addAll(chestShopItemLore)
                    add(enchantsHeader)
                    addAll(chestShopItemEnchantsComponent)
                    if (hasTeleportPermission) {
                        addAll(clickToTeleport)
                    }
                }

                // Setting the lore takes the most time in this process
                val item = ItemStack(icon)
                    .meta {
                        displayName(signLocation.toFormattedString().toComponent())
                        lore(mutableLore)
                    }

                pagination.addItem(IntelligentItem.of(item) {
                    if (hasTeleportPermission) {
                        if (chestShop.signLocation.isSafe()) {
                            player.teleportAsync(chestShop.signLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
                        } else {
                            val tpAnywayClickEvent = ClickEvent.callback({
                                val uuid = it[Identity.UUID].getOrNull() ?: return@callback
                                plugin.server.getPlayer(uuid)?.teleportAsync(chestShop.signLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
                            }, ClickCallback.Options.builder().lifetime(Duration.ofMinutes(1L)).build())
                            val teleportIsUnsafeMessage = "Teleport location is unsafe!".toComponent(NamedTextColor.RED).append(Component.space())
                                .append("[Click here to teleport anyway]".toComponent(NamedTextColor.DARK_RED).clickEvent(tpAnywayClickEvent))
                            player.sendMessage(teleportIsUnsafeMessage)
                        }
                    }
                })
            }
        }

        pagination.itemsPerPage = 45
        pagination.iterator(SlotIterator.builder().startPosition(0).type(SlotIterator.SlotIteratorType.HORIZONTAL).build())

        contents[45] = IntelligentItem.of(Constants.previousPageButton.name("<yellow>⇽ <gold>Previous Page".parse())) {
            if (!pagination.isFirst) {
                inventory.open(player, pagination.previous().page())
            }
        }

        contents[46] = IntelligentItem.of(ItemStack(Material.COMPASS).name("Search ChestShop")) {
            inventory.close(player)
            val playerChatInputBuilder = Utils.createChatInputBuilderBase(plugin, player)
                .isValidInput { _, input -> input.isNotEmpty() }
                .onFinish { player, input ->
                    player.clearTitle()
                    inventory.open(player, mapOf("chestshop_search_input" to input))
                }
            val playerChatInput = playerChatInputBuilder.build()
            playerChatInput.start()
            val title = Title.title("<red><bold>Search ChestShop".parse(), "<red>Search by player, item or container type!".parse(),
                Title.Times.times(Ticks.duration(10), Duration.ofSeconds(6000), Ticks.duration(20)))
            player.showTitle(title)
        }

        contents[49] = IntelligentItem.of(ItemStack(Material.IRON_DOOR).name("<red>Close".parse())) {
            inventory.close(player)
        }

        contents[53] = IntelligentItem.of(Constants.nextPageButton.name("<gold>Next Page <yellow>⇾".parse())) {
            if (!pagination.isLast) {
                inventory.open(player, pagination.next().page())
            }
        }
    }
}