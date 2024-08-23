package nl.chimpgamer.betterchestshops.paper.commands

import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.menus.MyChestShopsMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.CommandManager
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

class MyChestShopsCommand(private val plugin: BetterChestShopsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        commandManager.command(commandManager.commandBuilder(name, *aliases)
            .permission("betterchestshops.command.mychestshops")
            .senderType(Player::class.java)
            .optional("page", integerParser(), DefaultValue.constant(1))
            .handler { context ->
                val sender = context.sender()
                val page = context.get<Int>("page")

                MyChestShopsMenu(plugin).inventory.open(sender, page)
            }
        )
    }
}