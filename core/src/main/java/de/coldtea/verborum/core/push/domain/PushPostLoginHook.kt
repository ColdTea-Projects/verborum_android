package de.coldtea.verborum.core.push.domain

import de.coldtea.verborum.core.auth.domain.PostLoginHook
import javax.inject.Inject

/** Registers this install for push once a session exists. Scheduled, so a failing call never strands login. */
class PushPostLoginHook @Inject constructor(
    private val scheduler: PushRegistrationScheduler,
) : PostLoginHook {

    override suspend fun onLoginCompleted(subject: String) = scheduler.requestRegistration()
}
