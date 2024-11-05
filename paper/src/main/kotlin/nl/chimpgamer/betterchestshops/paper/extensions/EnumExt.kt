package nl.chimpgamer.betterchestshops.paper.extensions

inline fun <reified T: Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

inline fun <reified T: Enum<T>> T.previous(): T {
    val values = enumValues<T>()
    val previousOrdinal = if ((ordinal - 1) < 0) values.size -1 else (ordinal-1)
    return values[previousOrdinal]
}