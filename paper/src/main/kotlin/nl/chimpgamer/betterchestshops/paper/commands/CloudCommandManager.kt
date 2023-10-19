package nl.chimpgamer.betterchestshops.paper.commands

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.exceptions.NoPermissionException
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.parse
import org.bukkit.command.CommandSender
import java.util.function.Function
import java.util.logging.Level

class CloudCommandManager(private val plugin: BetterChestShopsPlugin) {

    private lateinit var paperCommandManager: PaperCommandManager<CommandSender>

    fun initialize() {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()

        try {
            paperCommandManager = PaperCommandManager(
                plugin.bootstrap,
                executionCoordinatorFunction,
                Function.identity(),
                Function.identity()
            )

            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                paperCommandManager.registerBrigadier()
                val brigadierManager = paperCommandManager.brigadierManager()
                brigadierManager?.setNativeNumberSuggestions(false)
            }
            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                paperCommandManager.registerAsynchronousCompletions()
            }

            MinecraftExceptionHandler<CommandSender>()
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler()
                .withCommandExecutionHandler()
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION) { e ->
                    e as NoPermissionException
                    plugin.messagesConfig.noPermission.parse(Placeholder.parsed("missing_permission", e.missingPermission))
                }
                .apply(paperCommandManager) { it }
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize the command manager", ex)
        }
    }

    fun loadCommands() {
        BetterChestShopsCommand(plugin).registerCommands(paperCommandManager, "betterchestshops", "bcs")
        MyChestShopsCommand(plugin).registerCommands(paperCommandManager, "mychestshops", "mychestshop")
    }
}