package nl.chimpgamer.betterchestshops.paper.handlers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin
import nl.chimpgamer.betterchestshops.paper.storage.tables.ChestShopsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.concurrent.Executors

class DatabaseHandler(private val plugin: BetterChestShopsPlugin) {
    private lateinit var database: Database

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

    var databaseDispatcher = Dispatchers.IO

    private fun connect() {
        val databaseFile = plugin.dataFolder.resolve("data.db")
        val settings = plugin.settingsConfig
        val storageType = settings.storageType.lowercase()

        if (storageType == "sqlite") {
            databaseDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            database = Database.connect("jdbc:sqlite:${databaseFile.absolutePath}", databaseConfig = DatabaseConfig {
                defaultMinRepetitionDelay = 100L
                defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            })
        } else if (storageType == "mysql" || storageType == "mariadb") {
            val host = settings.storageHost
            val port = settings.storagePort
            val databaseName = settings.storageDatabase
            val username = settings.storageUsername
            val password = settings.storagePassword
            val properties = settings.storageProperties

            var url = "jdbc:$storageType://$host:$port/$databaseName"
            if (properties.isNotEmpty()) {
                url += "?" + properties.map { "${it.key}=${it.value}" }.joinToString("&")
            }

            database = Database.connect(
                url,
                user = username,
                password = password,
                databaseConfig = DatabaseConfig {
                    defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                }
            )
        }
    }

    fun initialize() {
        connect()
        if (isDatabaseInitialized) {
            transaction {
                SchemaUtils.create(ChestShopsTable)
            }
        }
    }

    fun close() = TransactionManager.closeAndUnregister(database)
}