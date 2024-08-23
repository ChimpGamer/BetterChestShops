import java.util.*

val exposedVersion = "0.52.0"

plugins {
    kotlin("jvm") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "nl.chimpgamer.betterchestshops"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        maven("https://repo.minebench.de") // ChestShop Repository

        maven("https://repo.codemc.io/repository/maven-public/") // HolographicDisplays, BentoBox Repository

        maven("https://jitpack.io") // DecentHolograms Repository

        maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository

        maven("https://repo.fancyplugins.de/releases") // FancyHolograms Repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("dev.dejvokep:boosted-yaml:1.3.7")
        compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-java-time:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.xerial:sqlite-jdbc:3.46.0.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
        compileOnly("com.zaxxer:HikariCP:5.1.0")

        compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
        compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
        compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
        compileOnly("world.bentobox:bentobox:2.0.0-SNAPSHOT")
        compileOnly("de.oliver:FancyHolograms:2.3.3")

        compileOnly(fileTree("../libs"))
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }

        processResources {
            filesMatching("**/*.yml") {
                expand("version" to project.version)
            }
        }

        shadowJar {
            val buildNumber = System.getenv("BUILD_NUMBER")
            if (buildNumber == null) {
                archiveFileName.set("BetterChestShops-${project.name.capitalizeWords()}-v${project.version}.jar")
            } else {
                archiveFileName.set("BetterChestShops-${project.name.capitalizeWords()}-v${project.version}-b$buildNumber.jar")
            }

            //relocate("de.tr7zw")
            /*relocate("net.kyori.adventure.text.feature.pagination")*/
            relocate("org.bstats", "nl.chimpgamer.betterchestshops.shaded.bstats")
            relocate("com.github.shynixn.mccoroutine", "nl.chimpgamer.betterchestshops.shaded.mccoroutine")
            relocate("io.github.rysefoxx.inventory", "nl.chimpgamer.betterchestshops.shaded.ryseinventory")
        }

        build {
            dependsOn(shadowJar)
        }

        jar {
            enabled = false
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        enabled = false
    }
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}