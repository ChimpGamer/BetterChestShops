package nl.chimpgamer.betterchestshops.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.IntegerArgument
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.menus.MyChestShopsMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MyChestShopsCommand(private val plugin: BetterChestShopsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val pageArgument = IntegerArgument.optional<CommandSender>("page")

        commandManager.command(commandManager.commandBuilder(name, *aliases)
            .permission("betterchestshops.command.mychestshops")
            .senderType(Player::class.java)
            .argument(pageArgument)
            .handler { context ->
                val sender = context.sender as Player
                val page = context.getOptional(pageArgument).orElse(1)

                MyChestShopsMenu(plugin).inventory.open(sender, page)
            }
        )
    }
}