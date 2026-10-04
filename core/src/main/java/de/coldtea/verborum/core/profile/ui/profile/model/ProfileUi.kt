package de.coldtea.verborum.core.profile.ui.profile.model

/** What the profile page and the Forum gate need to know about the signed-in user. */
data class ProfileUi(
    val email: String,
    // Empty when no name is set.
    val displayName: String,
    val isMarketplaceMember: Boolean,
)
