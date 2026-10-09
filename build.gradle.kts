import java.text.SimpleDateFormat
import java.util.*

val exposedVersion = "0.59.0"

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

allprojects {
    group = "nl.chimpgamer.betterchestshops"
    version = "1.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.gradleup.shadow")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    kotlin {
        jvmToolchain(21)
    }

    tasks {
        processResources {
            filesMatching("**/*.yml") {
                val buildNumber = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"
                expand("version" to project.version, "buildDate" to getDate(), "buildNumber" to buildNumber)
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
    jar {
        enabled = false
    }
}

fun getDate(): String {
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    val date = Date()
    return simpleDateFormat.format(date)
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}