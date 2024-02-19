package nl.chimpgamer.betterchestshops.paper

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import nl.chimpgamer.betterchestshops.paper.commands.CloudCommandManager
import nl.chimpgamer.betterchestshops.paper.configurations.MessagesConfig
import nl.chimpgamer.betterchestshops.paper.configurations.SettingsConfig
import nl.chimpgamer.betterchestshops.paper.handlers.ChestShopsHandler
import nl.chimpgamer.betterchestshops.paper.handlers.DatabaseHandler
import nl.chimpgamer.betterchestshops.paper.hooks.HookManager
import nl.chimpgamer.betterchestshops.paper.listeners.BentoBoxListener
import nl.chimpgamer.betterchestshops.paper.listeners.ChestShopListener
import nl.chimpgamer.betterchestshops.paper.managers.HologramManager
import nl.chimpgamer.betterchestshops.paper.tasks.ChestShopIconTask
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class BetterChestShopsPlugin(val bootstrap: Bootstrap) {
    val consoleUUID: UUID = UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670")

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val databaseHandler = DatabaseHandler(this)
    val chestShopsHandler = ChestShopsHandler(this)
    val hologramManager = HologramManager(this)
    val cloudCommandManager = CloudCommandManager(this)
    val inventoryManager get() = bootstrap.inventoryManager

    val chestShopIconTask = ChestShopIconTask(this)

    private val hookManager = HookManager(this)

    fun load() {
        instance = this
        // Make sure that the BetterChestShops folder exists.
        try {
            val dataFolderPath = dataFolder.toPath()
            if (!Files.isDirectory(dataFolderPath)) {
                Files.createDirectories(dataFolderPath)
            }
        } catch (ex: IOException) {
            logger.log(Level.SEVERE, "Unable to create plugin directory", ex)
        }
    }

    fun enable() {
        databaseHandler.initialize()

        chestShopsHandler.load()

        hologramManager.initialize()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        hookManager.load()

        val pluginManager = server.pluginManager
        pluginManager.registerSuspendingEvents(ChestShopListener(this), this.bootstrap)


        if (pluginManager.isPluginEnabled("BentoBox")) {
            pluginManager.registerSuspendingEvents(BentoBoxListener(this), this.bootstrap)
        }

        launch(Dispatchers.IO) {
            while (true) {
                chestShopIconTask.run()
                delay(settingsConfig.hologramRefreshInterval.seconds)
            }
        }
    }

    fun disable() {
        hookManager.unload()
        HandlerList.unregisterAll(bootstrap)
        server.scheduler.cancelTasks(this.bootstrap)
        if (databaseHandler.isDatabaseInitialized) {
            databaseHandler.close()
        }
    }

    val server get() = bootstrap.server

    val dataFolder get() = bootstrap.dataFolder

    val logger get() = bootstrap.logger

    @Suppress("DEPRECATION")
    val version get() = bootstrap.description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = bootstrap.description.authors

    fun getResource(filename: String) = bootstrap.getResource(filename)

    fun registerEvents(
        vararg listeners: Listener
    ) = listeners.forEach { server.pluginManager.registerEvents(it, bootstrap) }

    fun launch(
        context: CoroutineContext = bootstrap.minecraftDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = bootstrap.launch(context, start, block)

    fun runSync(runnable: Runnable) {
        if (server.isPrimaryThread) {
            runnable.run()
        } else {
            server.scheduler.runTask(bootstrap, runnable)
        }
    }

    fun runAsync(runnable: Runnable) {
        if (!server.isPrimaryThread) {
            runnable.run()
        } else {
            server.scheduler.runTaskAsynchronously(bootstrap, runnable)
        }
    }

    fun callEvent(event: Event) = server.pluginManager.callEvent(event)

    fun callEventSync(event: Event) = runSync { callEvent(event) }

    fun debug(message: () -> Any) {
        if (!settingsConfig.debug) return
        if (message is Component) {
            bootstrap.componentLogger.info(message)
        } else {
            logger.info(message.toString())
        }
    }

    fun hasReachedLimit(player: Player): Boolean {
        val chestShopLimit = getChestShopLimit(player)
        if (chestShopLimit == -1) return false
        if (chestShopLimit == 0) return true
        if (chestShopLimit > 0) {
            val chestShops = chestShopsHandler.getAllByCreator(player.uniqueId).count()
            if (chestShops >= chestShopLimit) {
                player.sendRichMessage("<red>You've reached the maximum limit of <yellow>$chestShops <red>chestshops!")
                return true
            }
        }
        return false
    }

    fun getChestShopLimit(player: Player): Int {
        if (player.hasPermission("betterchestshops.shoplimit.*")) {
            return -1
        }
        val limits: MutableSet<Int> = HashSet()
        for (permissionAttachmentInfo in player.effectivePermissions) {
            val permission = permissionAttachmentInfo.permission
            if (permission.startsWith(
                    "betterchestshops.shoplimit.",
                    ignoreCase = true
                ) && permissionAttachmentInfo.value
            ) {
                val amount = permission.replace("betterchestshops.shoplimit.", "")
                limits.add(amount.toIntOrNull() ?: 0)
            }
        }
        return limits.maxByOrNull { it } ?: 0
    }

    companion object {
        lateinit var instance: BetterChestShopsPlugin
            private set
    }
}