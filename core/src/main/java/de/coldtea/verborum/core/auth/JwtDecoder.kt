package de.coldtea.verborum.core.auth

import android.util.Base64
import de.coldtea.verborum.core.extensions.json
import de.coldtea.verborum.core.extensions.stringOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject

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

    /** The Keycloak subject — the identity every service stores as the row owner (guide §4). */
    fun subject(jwt: String?): String? = claims(jwt)?.stringOrNull("sub")

    fun email(jwt: String?): String? = claims(jwt)?.stringOrNull("email")

    /**
     * The `email_verified` claim. Keycloak now requires a verified address before an account is
     * usable, so an unverified subject must not be let into the app shell. Absent claim → true:
     * the realm may hand out tokens without it (social sign-in, a realm with verification off) and
     * a missing claim must never lock out an otherwise valid session.
     */
    fun emailVerified(jwt: String?): Boolean =
        (claims(jwt)?.get("email_verified") as? JsonPrimitive)?.booleanOrNull ?: true

    fun displayName(jwt: String?): String? {
        val claims = claims(jwt) ?: return null
        return claims.stringOrNull("name") ?: claims.stringOrNull("preferred_username")
    }
}
