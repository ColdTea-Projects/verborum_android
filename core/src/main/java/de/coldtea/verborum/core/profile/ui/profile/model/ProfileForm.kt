package de.coldtea.verborum.core.profile.ui.profile.model

/**
 * The profile page's editable state: the saved values from `/users/me` plus the user's drafts.
 * Holds the rules the Update button follows (guide §4.4, §6.2).
 */
data class ProfileForm(
    val email: String,
    val savedDisplayName: String,
    val savedAgreementAccepted: Boolean,
    val displayName: String,
    val agreementAccepted: Boolean,
    val isSaving: Boolean = false,
    val isLeaveConfirmationShown: Boolean = false,
) {
    // The server trims names, so whitespace-only edits are not a change.
    val trimmedDisplayName: String get() = displayName.trim()

    val hasChanges: Boolean
        get() = trimmedDisplayName != savedDisplayName || agreementAccepted != savedAgreementAccepted

    /** A Forum member must have a name: the terms cannot be accepted while it is blank. */
    val isDisplayNameMissing: Boolean get() = agreementAccepted && trimmedDisplayName.isEmpty()

    /** Unticking the terms of a current member leaves the Forum — confirmed first (guide §6.2). */
    val isLeavingForum: Boolean get() = savedAgreementAccepted && !agreementAccepted

    val canUpdate: Boolean get() = hasChanges && !isDisplayNameMissing && !isSaving

    companion object {
        fun from(profile: ProfileUi) = ProfileForm(
            email = profile.email,
            savedDisplayName = profile.displayName,
            savedAgreementAccepted = profile.isMarketplaceMember,
            displayName = profile.displayName,
            agreementAccepted = profile.isMarketplaceMember,
        )
    }
}
