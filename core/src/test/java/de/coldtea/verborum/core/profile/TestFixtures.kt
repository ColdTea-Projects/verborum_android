package de.coldtea.verborum.core.profile

import de.coldtea.verborum.core.profile.data.api.model.UserProfileResponse
import de.coldtea.verborum.core.profile.domain.model.UserProfile
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileForm
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi

fun testUserProfileResponse(
    id: String = "4140c0de-0000-4000-8000-0000000000bb",
    email: String? = "anna@example.com",
    displayName: String? = "Anna",
    marketplaceAgreementAccepted: Boolean = false,
    marketplaceAgreementVersion: String? = null,
) = UserProfileResponse(
    id = id,
    email = email,
    displayName = displayName,
    marketplaceAgreementAccepted = marketplaceAgreementAccepted,
    marketplaceAgreementVersion = marketplaceAgreementVersion,
)

fun testUserProfile(
    userId: String = "4140c0de-0000-4000-8000-0000000000bb",
    email: String = "anna@example.com",
    displayName: String? = "Anna",
    isMarketplaceMember: Boolean = false,
    marketplaceAgreementVersion: String? = null,
) = UserProfile(
    userId = userId,
    email = email,
    displayName = displayName,
    isMarketplaceMember = isMarketplaceMember,
    marketplaceAgreementVersion = marketplaceAgreementVersion,
)

fun testProfileUi(
    email: String = "anna@example.com",
    displayName: String = "Anna",
    isMarketplaceMember: Boolean = false,
) = ProfileUi(
    email = email,
    displayName = displayName,
    isMarketplaceMember = isMarketplaceMember,
)

fun testProfileForm(
    savedDisplayName: String = "Anna",
    savedAgreementAccepted: Boolean = false,
    displayName: String = savedDisplayName,
    agreementAccepted: Boolean = savedAgreementAccepted,
    isSaving: Boolean = false,
) = ProfileForm(
    email = "anna@example.com",
    savedDisplayName = savedDisplayName,
    savedAgreementAccepted = savedAgreementAccepted,
    displayName = displayName,
    agreementAccepted = agreementAccepted,
    isSaving = isSaving,
)
