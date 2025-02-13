import java.text.SimpleDateFormat
import java.util.*

val exposedVersion = "0.57.0"

plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.5"
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
        plugin("com.gradleup.shadow")
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
        compileOnly("org.xerial:sqlite-jdbc:3.48.0.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.1")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
        compileOnly("com.zaxxer:HikariCP:6.2.1")

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
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val date = Date()
    return simpleDateFormat.format(date)
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}