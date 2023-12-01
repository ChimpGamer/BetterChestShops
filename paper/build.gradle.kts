repositories {
    maven("https://papermc.io/repo/repository/maven-public/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.12.1") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.12.1") { isTransitive = false }

    implementation("org.bstats:bstats-bukkit:3.0.2")
}