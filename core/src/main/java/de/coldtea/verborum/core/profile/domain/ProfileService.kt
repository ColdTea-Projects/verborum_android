package de.coldtea.verborum.core.profile.domain

import de.coldtea.verborum.core.profile.data.api.model.ProfileInfoRequest
import de.coldtea.verborum.core.profile.domain.model.MarketplaceTerms
import de.coldtea.verborum.core.profile.domain.model.UserProfile
import de.coldtea.verborum.core.profile.domain.usecase.api.EnsureUserProfileUseCase
import de.coldtea.verborum.core.profile.domain.usecase.local.GetSessionIdentityUseCase
import de.coldtea.verborum.core.profile.domain.usecase.api.UpdateProfileInfoApiUseCase
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The signed-in user's profile, held for the session (guide §8.1): the profile screen and the
 * Forum gate both observe it, and every successful update refreshes it from `GET /users/me`.
 * Online-only — profile changes are never queued (guide §6.9). Cleared on logout.
 */
@Singleton
class ProfileService @Inject constructor(
    private val ensureUserProfileUseCase: EnsureUserProfileUseCase,
    private val updateProfileInfoApiUseCase: UpdateProfileInfoApiUseCase,
    private val getSessionIdentityUseCase: GetSessionIdentityUseCase,
) {
    private val profile = MutableStateFlow<UserProfile?>(null)

    /** The last loaded profile; null until [refreshProfile] first succeeds. */
    fun observeProfile(): Flow<ProfileUi?> =
        profile
            .map { it?.convertToUi() }
            .distinctUntilChanged()

    /** Reloads from `/users/me`, creating the profile if the account has none. Throws on failure. */
    suspend fun refreshProfile(): ProfileUi {
        val identity = getSessionIdentityUseCase.invoke()
            ?: throw IllegalStateException("Not signed in")
        val loaded = ensureUserProfileUseCase.invoke(
            subject = identity.subject,
            email = identity.email,
            displayName = identity.displayName,
        )
        profile.value = loaded
        return loaded.convertToUi()
    }

    /**
     * Sends only what changed — a null argument is left unchanged on the server. Accepting the
     * terms always carries the current [MarketplaceTerms.VERSION]; `displayName = ""` removes the
     * name. Throws [de.coldtea.verborum.core.profile.domain.model.InvalidProfileException] when a
     * profile rule is broken.
     */
    suspend fun updateProfile(displayName: String?, marketplaceAgreementAccepted: Boolean?): ProfileUi {
        updateProfileInfoApiUseCase.invoke(
            ProfileInfoRequest(
                displayName = displayName,
                marketplaceAgreementAccepted = marketplaceAgreementAccepted,
                marketplaceAgreementVersion = MarketplaceTerms.VERSION.takeIf { marketplaceAgreementAccepted == true },
            )
        )
        return refreshProfile()
    }

    /** Forgets the profile — the next account on this device must never see it. */
    fun clear() {
        profile.value = null
    }
}
