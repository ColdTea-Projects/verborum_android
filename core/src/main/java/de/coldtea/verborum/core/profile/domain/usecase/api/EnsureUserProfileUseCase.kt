package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.data.api.model.UserProfileRequest
import de.coldtea.verborum.core.profile.domain.model.UserProfile
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

/**
 * Loads the caller's profile, creating it first if the account has none (guide §4.2–§4.3, §6.1):
 * `GET /users/me`, and only on a **404** `POST /users/` with a fresh random `userId`, then `/me`
 * again. A 409 on create means a profile appeared meanwhile (another device, a retried login) —
 * it is recovered by the second `/me`, never by posting again. Any other failure throws.
 */
class EnsureUserProfileUseCase @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun invoke(subject: String, email: String?, displayName: String?): UserProfile {
        val existing = userApi.getMe()
        existing.body()?.takeIf { existing.isSuccessful }?.let { return it.convertToProfile() }
        if (existing.code() != HTTP_NOT_FOUND) throw HttpException(existing)

        // ms_user rejects a profile without a valid address; nothing to create it from.
        val address = email?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException("No email claim to create the profile from")

        val created = userApi.createProfile(
            UserProfileRequest(
                userId = UUID.randomUUID().toString(),
                keycloakId = subject,
                email = address,
                displayName = displayName?.trim()?.takeIf(String::isNotEmpty)?.take(MAX_DISPLAY_NAME_LENGTH),
            )
        )
        if (!created.isSuccessful && created.code() != HTTP_CONFLICT) throw HttpException(created)

        val reloaded = userApi.getMe()
        return reloaded.body()?.takeIf { reloaded.isSuccessful }?.convertToProfile()
            ?: throw HttpException(reloaded)
    }

    companion object {
        /** ms_user's limit on `displayName` (guide §4.4). */
        const val MAX_DISPLAY_NAME_LENGTH = 255

        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_CONFLICT = 409
    }
}
