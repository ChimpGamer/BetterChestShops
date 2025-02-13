package nl.chimpgamer.betterchestshops.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.betterchestshops.paper.BetterChestShopsPlugin

class MessagesConfig(plugin: BetterChestShopsPlugin) {
    val config: YamlDocument

    val noPermission: String get() = config.getString("noPermission")

    val maximumLimitReached: String get() = config.getString("shop.create.maximum-limit-reached")

    val shopCreateError: String get() = config.getString("shop.create.error")
    val shopCreateUnknownContainerTypes: String get() = config.getString("shop.create.unknown-container-types")

    init {
        val file = plugin.dataFolder.resolve("messages.yml")
        val inputStream = plugin.getResource("messages.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}