package de.coldtea.verborum.core.extensions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@ExperimentalSerializationApi
val json = Json{
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}

/** The string value of [key], or null when it is absent or not a primitive. */
fun JsonObject.stringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull
