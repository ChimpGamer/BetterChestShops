repositories {
    maven("https://papermc.io/repo/repository/maven-public/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")

    compileOnly("org.incendo:cloud-core:2.0.0-rc.2")
    compileOnly("org.incendo:cloud-paper:2.0.0-beta.9")
    compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
    compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0-rc.2")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.19.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.19.0") { isTransitive = false }

    implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.13")

    implementation("org.bstats:bstats-bukkit:3.0.2")
}