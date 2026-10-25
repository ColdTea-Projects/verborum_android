package de.coldtea.verborum.core.profile.domain

import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.profile.domain.usecase.api.DeleteAccountApiUseCase
import javax.inject.Inject

/**
 * Deletes the signed-in account (`docs/account-deletion-guide.md`). The order is fixed: the server
 * deletes first, and the local wipe runs only after it answered 200 — an unknown result must never
 * wipe the phone (guide §8).
 *
 * Kept apart from [ProfileService] on purpose: [AuthService] runs hooks that depend on
 * [ProfileService], so [ProfileService] cannot depend on [AuthService].
 */
class AccountDeletionService @Inject constructor(
    private val profileService: ProfileService,
    private val deleteAccountApiUseCase: DeleteAccountApiUseCase,
    private val authService: AuthService,
) {
    /**
     * Throws [de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException] when the
     * login is too old, and any other exception when the result is unknown; local data stays in both cases.
     */
    suspend fun deleteAccount() {
        deleteAccountApiUseCase.invoke(profileService.getUserId())
        authService.completeAccountDeletion()
    }
}
