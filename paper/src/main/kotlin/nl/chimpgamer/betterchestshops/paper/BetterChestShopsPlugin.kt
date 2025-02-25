package nl.chimpgamer.betterchestshops.paper

import com.Acrobot.ChestShop.Events.PreShopCreationEvent
import com.Acrobot.ChestShop.Events.ShopCreatedEvent
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent
import com.github.shynixn.mccoroutine.folia.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.betterchestshops.paper.commands.CloudCommandManager
import nl.chimpgamer.betterchestshops.paper.configurations.MessagesConfig
import nl.chimpgamer.betterchestshops.paper.configurations.SettingsConfig
import nl.chimpgamer.betterchestshops.paper.extensions.parse
import nl.chimpgamer.betterchestshops.paper.handlers.ChestShopsHandler
import nl.chimpgamer.betterchestshops.paper.handlers.DatabaseHandler
import nl.chimpgamer.betterchestshops.paper.hooks.HookManager
import nl.chimpgamer.betterchestshops.paper.listeners.BentoBoxListener
import nl.chimpgamer.betterchestshops.paper.listeners.ChestShopListener
import nl.chimpgamer.betterchestshops.paper.managers.HologramManager
import nl.chimpgamer.betterchestshops.paper.tasks.ChestShopIconTask
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import world.bentobox.bentobox.api.events.island.IslandPreclearEvent
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

    var buildNumber: String = ""
    var buildDate: String = ""

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

        loadPluginInfo()
    }

    fun enable() {
        databaseHandler.initialize()

        chestShopsHandler.load()

        hologramManager.initialize()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        hookManager.load()

        val pluginManager = server.pluginManager
        val eventDispatcher = mapOf<Class<out Event>, (event: Event) -> CoroutineContext>(
            Pair(MCCoroutineExceptionEvent::class.java) {
                require(it is MCCoroutineExceptionEvent)
                bootstrap.globalRegionDispatcher
            },
            Pair(ShopCreatedEvent::class.java) {
                require(it is ShopCreatedEvent)
                bootstrap.entityDispatcher(it.player)
            },
            Pair(ShopDestroyedEvent::class.java) {
                require(it is ShopDestroyedEvent)
                val destroyer = it.destroyer
                if (destroyer != null) bootstrap.entityDispatcher(destroyer) else bootstrap.globalRegionDispatcher
            },
            Pair(BlockBreakEvent::class.java) {
                require(it is BlockBreakEvent)
                bootstrap.entityDispatcher(it.player)
            },
            Pair(BlockPistonExtendEvent::class.java) {
                require(it is BlockPistonExtendEvent)
                bootstrap.regionDispatcher(it.block.location)
            },
            Pair(BlockPlaceEvent::class.java) {
                require(it is BlockPlaceEvent)
                bootstrap.regionDispatcher(it.block.location)
            },
            Pair(ChunkLoadEvent::class.java) {
                require(it is ChunkLoadEvent)
                bootstrap.regionDispatcher(it.world, it.chunk.x, it.chunk.z)
            },
            Pair(BlockFromToEvent::class.java) {
                require(it is BlockFromToEvent)
                bootstrap.regionDispatcher(it.block.location)
            },
            Pair(PreShopCreationEvent::class.java) {
                require(it is PreShopCreationEvent)
                bootstrap.entityDispatcher(it.player)
            },
        )

        pluginManager.registerSuspendingEvents(ChestShopListener(this), bootstrap, eventDispatcher)


        if (pluginManager.isPluginEnabled("BentoBox")) {
            pluginManager.registerSuspendingEvents(BentoBoxListener(this), bootstrap, mapOf(
                Pair(MCCoroutineExceptionEvent::class.java) {
                    require(it is MCCoroutineExceptionEvent)
                    bootstrap.globalRegionDispatcher
                },
                Pair(IslandPreclearEvent::class.java) {
                    require(it is IslandPreclearEvent)
                    bootstrap.globalRegionDispatcher
                },
            ))
        }

        launch(bootstrap.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            while (true) {
                chestShopIconTask.run()
                delay(settingsConfig.hologramRefreshInterval.seconds)
            }
        }

        databaseHandler.cleanupBackups()
    }

    fun disable() {
        hookManager.unload()
        HandlerList.unregisterAll(bootstrap)
        server.scheduler.cancelTasks(bootstrap)
        if (databaseHandler.isDatabaseInitialized) {
            databaseHandler.close()

            databaseHandler.backupSQLiteDatabase()
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

    fun launch(
        context: CoroutineContext = bootstrap.globalRegionDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = bootstrap.launch(context, start, block)

    fun debug(message: () -> Any) {
        if (!settingsConfig.debug) return
        message().run {
            if (this is Component) {
                bootstrap.componentLogger.info(this)
            } else {
                logger.info(this.toString())
            }
        }
    }

    fun hasReachedLimit(player: Player): Boolean {
        val chestShopLimit = getChestShopLimit(player)
        if (chestShopLimit == -1) return false
        if (chestShopLimit == 0) return true
        if (chestShopLimit > 0) {
            val chestShops = chestShopsHandler.getCountByCreator(player.uniqueId)
            if (chestShops >= chestShopLimit) {
                player.sendMessage(
                    messagesConfig.maximumLimitReached.parse(Placeholder.unparsed("limit", chestShopLimit.toString()))
                )
                return true
            }
        }
        return false
    }

    fun getChestShopLimit(player: Player): Int {
        val prefix = "betterchestshops.shoplimit."
        if (player.hasPermission("$prefix*")) {
            return -1
        }
        val limits: MutableSet<Int> = HashSet()
        for (permissionAttachmentInfo in player.effectivePermissions) {
            val permission = permissionAttachmentInfo.permission
            if (permission.startsWith(
                    prefix,
                    ignoreCase = true
                ) && permissionAttachmentInfo.value
            ) {
                val amount = permission.replace(prefix, "")
                limits.add(amount.toIntOrNull() ?: 0)
            }
        }
        return limits.maxByOrNull { it } ?: 0
    }

    private fun loadPluginInfo() {
        getResource("paper-plugin.yml")?.let {
            it.reader().use { reader ->
                val pluginYml = YamlConfiguration.loadConfiguration(reader)
                buildNumber = pluginYml.getString("build-number") ?: ""
                buildDate = pluginYml.getString("build-date") ?: ""
            }
        }
    }

    companion object {
        lateinit var instance: BetterChestShopsPlugin
            private set
    }
}