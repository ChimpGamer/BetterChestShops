package nl.chimpgamer.betterchestshops.paper.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

internal fun String.toComponent() = Component.text(this)
internal fun String.toComponent(namedTextColor: NamedTextColor) = Component.text(this, namedTextColor)

internal fun Int.toComponent() = Component.text(this)
internal fun Boolean.toComponent() = Component.text(this)

internal fun Component.toPlainText() = PlainTextComponentSerializer.plainText().serialize(this)