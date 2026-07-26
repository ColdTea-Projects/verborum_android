package de.coldtea.verborum.core.auth.domain.model

/** The tokens returned by a successful code exchange. */
data class TokenBundle(
    val accessToken: String?,
    val refreshToken: String?,
    val idToken: String?,
)
