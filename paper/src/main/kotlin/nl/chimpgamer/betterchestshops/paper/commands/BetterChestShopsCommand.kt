package nl.chimpgamer.betterchestshops.paper.commands

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.menus.ChestShopsMenu
import nl.chimpgamer.betterchestshops.paper.models.ChestShopSortBy
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.CommandManager
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

class BetterChestShopsCommand(private val plugin: BetterChestShopsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "betterchestshops.command"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        commandManager.command(builder
            .senderType(Player::class.java)
            .optional("page", integerParser(), DefaultValue.constant(1))
            .optional("sortby", enumParser(ChestShopSortBy::class.java), DefaultValue.constant(ChestShopSortBy.BUY_AND_SELL_PRICE))
            .handler { context ->
                val sender = context.sender()
                val page = context.get<Int>("page")
                val sortBy = context.get<ChestShopSortBy>("sortby")
                ChestShopsMenu(plugin).inventory.open(sender, page, mapOf("chestshop_sort_by" to sortBy))
            }
        )

        commandManager.command(builder
            .literal("about")
            .permission("$basePermission.about")
            .handler { context ->
                val sender = context.sender()
                sender.sendRichMessage("    <gold>BetterChestShops<br>" +
                        "<gray>Version > <yellow>${plugin.version}<br>" +
                        "<gray>Build Number > <yellow>${plugin.buildNumber}<br>" +
                        "<gray>Build Date > <yellow>${plugin.buildDate}<br>" +
                        "<gray>HologramHandler > <yellow>${plugin.hologramManager.hologramHandler.name}<br>" +
                        "<gray>ChestShops Registered > <yellow>${plugin.chestShopsHandler.getChestShopCount()}"
                )
            }
        )

        commandManager.command(builder
            .literal("reload")
            .permission("$basePermission.reload")
            .handler { context ->
                val sender = context.sender()
                plugin.settingsConfig.config.reload()
                plugin.hologramManager.reload()

                sender.sendMessage("Reload complete!")
            }
        )

        commandManager.command(builder
            .literal("clearinvalid")
            .permission("$basePermission.clearinvalid")
            .suspendingHandler(context = plugin.bootstrap.globalRegionDispatcher) { context ->
                val sender = context.sender()

                val toRemove = plugin.chestShopsHandler.getChestShops { it.isChunkLoaded && !it.isValid }.toSet()

                val count = plugin.chestShopsHandler.removeChestShops(toRemove)
                sender.sendRichMessage("<gold>You have deleted <yellow>${count.get()} <gold>invalid chestshops!")
            }
        )
    }
}