package nl.chimpgamer.betterchestshops.paper.handlers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.storage.tables.BetterChestShopsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.io.path.*

class DatabaseHandler(private val plugin: BetterChestShopsPlugin) {
    private lateinit var database: Database
    private val databaseFile = plugin.dataFolder.resolve("data.db")
    private val backupsPath = plugin.dataFolder.resolve("backups").toPath()

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

    val databaseDispatcher get() = plugin.bootstrap.asyncDispatcher

    private fun connect() {
        val settings = plugin.settingsConfig
        val storageType = settings.storageType.lowercase()

        if (storageType == "sqlite") {
            val hikariConfig = HikariConfig().apply {
                poolName = "BetterChestShops-pool"
                jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
                driverClassName = "org.sqlite.JDBC"
                maximumPoolSize = 1
                transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }
            database = Database.connect(HikariDataSource(hikariConfig), databaseConfig = DatabaseConfig {
                defaultMinRetryDelay = 100L
            })
        } else if (storageType == "mysql" || storageType == "mariadb") {
            val host = settings.storageHost
            val port = settings.storagePort
            val databaseName = settings.storageDatabase
            val username = settings.storageUsername
            val password = settings.storagePassword
            val properties = settings.storageProperties.toMutableMap()
            if (storageType == "mysql") {
                properties.apply {
                    putIfAbsent("cachePrepStmts", "true")
                    putIfAbsent("prepStmtCacheSize", "250")
                    putIfAbsent("prepStmtCacheSqlLimit", "2048")
                    putIfAbsent("useServerPrepStmts", "true")
                    putIfAbsent("useLocalSessionState", "true")
                    putIfAbsent("rewriteBatchedStatements", "true")
                    putIfAbsent("cacheResultSetMetadata", "true")
                    putIfAbsent("cacheServerConfiguration", "true")
                    putIfAbsent("elideSetAutoCommits", "true")
                    putIfAbsent("maintainTimeStats", "true")
                    putIfAbsent("alwaysSendSetIsolation", "false")
                    putIfAbsent("cacheCallableStmts", "true")
                }
            }

            var url = "jdbc:$storageType://$host:$port/$databaseName"
            if (properties.isNotEmpty()) {
                url += "?" + properties.map { "${it.key}=${it.value}" }.joinToString("&")
            }

            val hikariConfig = HikariConfig().apply {
                poolName = "UltimateMobCoins-pool"
                jdbcUrl = url
                driverClassName = if (storageType == "mysql") {
                    "com.mysql.cj.jdbc.Driver"
                } else {
                    "org.mariadb.jdbc.Driver"
                }
                this.username = username
                this.password = password
                this.maximumPoolSize = settings.storagePoolSettingsMaximumPoolSize
                this.minimumIdle = settings.storagePoolSettingsMinimumIdle
                this.maxLifetime = settings.storagePoolSettingsMaximumLifetime
                this.connectionTimeout = settings.storagePoolSettingsConnectionTimeout
                this.initializationFailTimeout = -1
            }

            database = Database.connect(HikariDataSource(hikariConfig))
        }
    }

    fun initialize() {
        connect()
        if (isDatabaseInitialized) {
            transaction {
                SchemaUtils.create(BetterChestShopsTable)
            }
        }
    }

    fun close() = TransactionManager.closeAndUnregister(database)

    fun backupSQLiteDatabase() {
        if (plugin.settingsConfig.storageType.lowercase() != "sqlite") return
        val fileName = "data-${Instant.now().toEpochMilli()}.db"
        if (!backupsPath.isDirectory()) {
            backupsPath.createDirectory();
        }
        databaseFile.toPath().copyTo(backupsPath.resolve(fileName))
    }

    fun cleanupBackups() {
        if (plugin.settingsConfig.storageType.lowercase() != "sqlite") return
        if (!backupsPath.isDirectory()) return
        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        backupsPath.filter { path -> path.isRegularFile() && path.getLastModifiedTime().toInstant().isBefore(sevenDaysAgo) }
            .forEach { path -> path.deleteIfExists() }
    }
}