package de.coldtea.verborum.core.profile.domain.model

import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi

/**
 * The signed-in user's ms_user profile (guide §4.2). [userId] is ms_user's own id — the one for
 * `/users/{userId}/…` paths — and never the token's `sub`.
 */
data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String?,
    // Forum member: has a display name and has accepted the marketplace terms (guide §4.1).
    val isMarketplaceMember: Boolean,
    val marketplaceAgreementVersion: String?,
) {
    fun convertToUi() = ProfileUi(
        email = email,
        displayName = displayName.orEmpty(),
        isMarketplaceMember = isMarketplaceMember,
    )
}
