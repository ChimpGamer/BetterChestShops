package nl.chimpgamer.betterchestshops.paper.commands

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.extensions.parse
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.caption.CaptionProvider
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.exception.InvalidSyntaxException
import org.incendo.cloud.exception.NoPermissionException
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter
import org.incendo.cloud.paper.LegacyPaperCommandManager
import java.util.logging.Level

class CloudCommandManager(private val plugin: BetterChestShopsPlugin) {

    private lateinit var paperCommandManager: LegacyPaperCommandManager<CommandSender>

    fun initialize() {
        try {
            paperCommandManager = LegacyPaperCommandManager.createNative(
                plugin.bootstrap,
                ExecutionCoordinator.asyncCoordinator()
            )

            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                paperCommandManager.registerBrigadier()
                val brigadierManager = paperCommandManager.brigadierManager()
                brigadierManager.setNativeNumberSuggestions(false)
            } else if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                paperCommandManager.registerAsynchronousCompletions()
            }

            paperCommandManager.captionRegistry().run {
                registerProvider(MinecraftHelp.defaultCaptionsProvider())
                registerProvider(CaptionProvider.constantProvider(StandardCaptionKeys.EXCEPTION_NO_PERMISSION, plugin.messagesConfig.noPermission))
            }

            MinecraftExceptionHandler.createNative<CommandSender>()
                .defaultHandlers()
                .captionFormatter(ComponentCaptionFormatter.miniMessage())
                .registerTo(paperCommandManager)
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize the command manager", ex)
        }
    }

    fun loadCommands() {
        BetterChestShopsCommand(plugin).registerCommands(paperCommandManager, "betterchestshops", "bcs")
        MyChestShopsCommand(plugin).registerCommands(paperCommandManager, "mychestshops", "mychestshop")
    }
}