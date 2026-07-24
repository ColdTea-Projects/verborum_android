package de.coldtea.verborum.core.auth

import android.util.Base64
import de.coldtea.verborum.core.extensions.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Reads standard claims out of a JWT without verifying its signature — the client only needs the
 * `sub`/`email`/name for identity and profile creation; the services do the real validation. Never
 * treat these claims as trusted for authorization decisions.
 */
@OptIn(ExperimentalSerializationApi::class)
object JwtDecoder {

    private fun claims(jwt: String?): JsonObject? {
        if (jwt.isNullOrBlank()) return null
        val parts = jwt.split(".")
        if (parts.size < 2) return null
        return runCatching {
            val decoded = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )
            json.parseToJsonElement(String(decoded, Charsets.UTF_8)).jsonObject
        }.getOrNull()
    }

    private fun JsonObject.string(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull

    /** The Keycloak subject — the identity every service stores as the row owner (guide §4). */
    fun subject(jwt: String?): String? = claims(jwt)?.string("sub")

    fun email(jwt: String?): String? = claims(jwt)?.string("email")

    fun displayName(jwt: String?): String? {
        val claims = claims(jwt) ?: return null
        return claims.string("name") ?: claims.string("preferred_username")
    }
}
