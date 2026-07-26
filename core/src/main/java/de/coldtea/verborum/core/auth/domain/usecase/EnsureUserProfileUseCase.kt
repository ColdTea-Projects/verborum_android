package de.coldtea.verborum.core.auth.domain.usecase

import de.coldtea.verborum.core.auth.data.api.UserApi
import de.coldtea.verborum.core.auth.data.api.model.UserProfileRequest
import javax.inject.Inject

/**
 * Ensures the ms_user profile exists after login. Safe rule from guide §3: create it only when
 * `GET /users/{id}` returns 404 — a network error leaves it for a later attempt rather than risking
 * a duplicate. The subject doubles as ms_user's `userId` so the lookup is idempotent across
 * re-installs.
 */
class EnsureUserProfileUseCase @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun invoke(subject: String, email: String?, displayName: String?) {
        val existing = runCatching { userApi.getProfile(subject) }.getOrNull() ?: return
        if (existing.isSuccessful) return
        if (existing.code() != HTTP_NOT_FOUND) return

        userApi.createProfile(
            UserProfileRequest(
                userId = subject,
                keycloakId = subject,
                email = email.orEmpty(),
                displayName = displayName.orEmpty(),
            )
        )
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
