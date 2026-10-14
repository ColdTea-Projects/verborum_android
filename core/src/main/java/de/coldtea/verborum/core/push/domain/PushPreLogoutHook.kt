package de.coldtea.verborum.core.push.domain

import de.coldtea.verborum.core.auth.domain.PreLogoutHook
import javax.inject.Inject

/** Tells the server to stop pushing to this install — the one step that needs the still-valid bearer. */
class PushPreLogoutHook @Inject constructor(
    private val pushRegistrationService: PushRegistrationService,
) : PreLogoutHook {

    override suspend fun onLoggingOut() = pushRegistrationService.unregister()
}
