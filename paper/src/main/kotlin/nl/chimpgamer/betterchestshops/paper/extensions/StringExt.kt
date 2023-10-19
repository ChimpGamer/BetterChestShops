package nl.chimpgamer.betterchestshops.paper.extensions

import java.util.*

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}