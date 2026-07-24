package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.core.auth.AuthTokenStore
import javax.inject.Inject

/**
 * The signed-in subject — the owner id stamped on dictionaries and words (guide §4). Null when no
 * one is logged in, in which case callers must not upload anything (the guest UUID must never reach
 * the server).
 */
class GetActiveUserUseCase @Inject constructor(
    private val tokenStore: AuthTokenStore,
) {
    fun invoke(): String? = tokenStore.currentUserId()
}
