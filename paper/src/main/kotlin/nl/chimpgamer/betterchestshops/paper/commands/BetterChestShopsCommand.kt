package nl.chimpgamer.betterchestshops.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.kotlin.coroutines.extension.suspendingHandler
import com.Acrobot.ChestShop.Signs.ChestShopSign
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.menus.ChestShopsMenu
import nl.chimpgamer.betterchestshops.paper.models.ChestShop
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BetterChestShopsCommand(private val plugin: BetterChestShopsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val pageArgument = IntegerArgument.optional<CommandSender>("page")

        val basePermission = "betterchestshops.command"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                val page = context.getOptional(pageArgument).orElse(1)
                ChestShopsMenu(plugin).inventory.open(sender, page)
            }
        )

        commandManager.command(builder
            .literal("reload")
            .permission("$basePermission.reload")
            .handler { context ->
                val sender = context.sender
                plugin.settingsConfig.config.reload()
                plugin.hologramManager.reload()

                sender.sendMessage("Reload complete!")
            }
        )

        commandManager.command(builder
            .literal("clearinvalid")
            .permission("$basePermission.clearinvalid")
            .suspendingHandler { context ->
                val sender = context.sender

                val toRemove = HashSet<ChestShop>()
                plugin.chestShopsHandler.getChestShopsUnordered().forEach { chestShop ->
                    if (!chestShop.isChunkLoaded) return@forEach
                    if (!ChestShopSign.isValid(chestShop.signLocation.block)) {
                        toRemove.add(chestShop)
                    }
                }

                val count = plugin.chestShopsHandler.removeChestShops(toRemove)
                sender.sendRichMessage("<gold>You have deleted <yellow>${count.get()} <gold>invalid chestshops!")
            }
        )
    }
}