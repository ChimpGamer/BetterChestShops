package nl.chimpgamer.betterchestshops.paper.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

private val legacyComponentSerializer = LegacyComponentSerializer.builder().character('&').hexColors().build()

fun Component.toLegacy() = legacyComponentSerializer.serialize(this)

fun String.toComponent() = Component.text(this)
fun String.toComponent(namedTextColor: NamedTextColor) = Component.text(this, namedTextColor)

fun Int.toComponent() = Component.text(this)
fun Boolean.toComponent() = Component.text(this)