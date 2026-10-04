package de.coldtea.verborum.core.profile.domain.model

/** The signed-in account as the token describes it — what a new profile is created from. */
data class SessionIdentity(
    val subject: String,
    val email: String?,
    val displayName: String?,
)
