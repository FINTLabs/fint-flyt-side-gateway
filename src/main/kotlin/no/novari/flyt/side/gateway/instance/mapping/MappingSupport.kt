package no.novari.flyt.side.gateway.instance.mapping

internal fun MutableMap<String, String>.putOrEmpty(
    key: String,
    value: Any?,
) {
    put(key, value?.toString() ?: "")
}
