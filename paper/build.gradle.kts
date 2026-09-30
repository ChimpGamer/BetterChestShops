repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.minebench.de") // ChestShop Repository

    maven("https://repo.codemc.io/repository/maven-public/") // HolographicDisplays, BentoBox Repository

    maven("https://jitpack.io") // DecentHolograms & GHolo Repository

    maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository

    maven("https://repo.fancyinnovations.com/releases") // FancyHolograms Repository
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
    compileOnly("world.bentobox:bentobox:2.4.0-SNAPSHOT")
    compileOnly("de.oliver:FancyHolograms:2.3.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.1.0")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:3.1.0")
    compileOnly("com.github.gecolay.GHolo:GHolo:2.3.0") { isTransitive = false }
    compileOnly("com.github.Zrips:CMI-API:9.8.6.4")

    compileOnly("org.incendo:cloud-core:2.1.0")
    compileOnly("org.incendo:cloud-paper:2.0.1")
    compileOnly("org.incendo:cloud-minecraft-extras:2.0.1")
    compileOnly("org.incendo:cloud-kotlin-coroutines:2.1.0")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.23.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.23.0") { isTransitive = false }

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