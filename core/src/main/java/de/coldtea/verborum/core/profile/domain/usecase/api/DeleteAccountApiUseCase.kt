package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.extensions.apiError
import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * `DELETE /users/{userId}` (`docs/account-deletion-guide.md` §2). A 403 whose `error` is
 * `ReauthenticationRequiredException` becomes [ReauthenticationRequiredException]; any other failure,
 * including another 403 (a client bug), throws [HttpException]. A 200 is also the answer for a profile
 * that is already gone, so a repeat is safe.
 */
class DeleteAccountApiUseCase @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun invoke(userId: String) {
        val response = userApi.deleteUser(userId)
        if (response.isSuccessful) return
        if (response.code() == HTTP_FORBIDDEN && response.apiError()?.error == REAUTHENTICATION_REQUIRED) {
            throw ReauthenticationRequiredException()
        }
        throw HttpException(response)
    }

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val REAUTHENTICATION_REQUIRED = "ReauthenticationRequiredException"
    }
}
