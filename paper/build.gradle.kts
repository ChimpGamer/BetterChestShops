repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.minebench.de") // ChestShop Repository

    maven("https://repo.codemc.io/repository/maven-public/") // HolographicDisplays, BentoBox Repository

    maven("https://jitpack.io") // DecentHolograms Repository

    maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository

    maven("https://repo.fancyplugins.de/releases") // FancyHolograms Repository
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
    compileOnly("world.bentobox:bentobox:2.4.0-SNAPSHOT")
    compileOnly("de.oliver:FancyHolograms:2.3.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.3.0")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.3.0")

    compileOnly("org.incendo:cloud-core:2.0.0")
    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")
    compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
    compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.20.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.20.0") { isTransitive = false }

    implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.14")

    implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks {
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}