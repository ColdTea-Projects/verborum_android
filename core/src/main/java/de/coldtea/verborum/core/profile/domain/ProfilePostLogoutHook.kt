package de.coldtea.verborum.core.profile.domain

import de.coldtea.verborum.core.auth.domain.PostLogoutHook
import javax.inject.Inject

/** Drops the cached profile on logout so the next account starts from a fresh `/users/me`. */
class ProfilePostLogoutHook @Inject constructor(
    private val profileService: ProfileService,
) : PostLogoutHook {
    override suspend fun onLoggedOut() = profileService.clear()
}
