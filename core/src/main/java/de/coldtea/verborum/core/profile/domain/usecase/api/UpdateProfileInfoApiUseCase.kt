package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.extensions.json
import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.data.api.model.ApiErrorResponse
import de.coldtea.verborum.core.profile.data.api.model.ProfileInfoRequest
import de.coldtea.verborum.core.profile.domain.model.InvalidProfileException
import kotlinx.serialization.ExperimentalSerializationApi
import retrofit2.HttpException
import javax.inject.Inject

/**
 * `PUT /users/me/profile-info` (guide §4.4). A 400 is a broken profile rule and becomes an
 * [InvalidProfileException] carrying the server's user-readable `errorDetail`; any other failure
 * throws [HttpException].
 */
class UpdateProfileInfoApiUseCase @Inject constructor(
    private val userApi: UserApi,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun invoke(request: ProfileInfoRequest) {
        val response = userApi.updateProfileInfo(request)
        if (response.isSuccessful) return
        if (response.code() == HTTP_BAD_REQUEST) {
            val detail = runCatching {
                json.decodeFromString<ApiErrorResponse>(response.errorBody()?.string().orEmpty()).errorDetail
            }.getOrNull()
            throw InvalidProfileException(detail)
        }
        throw HttpException(response)
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
    }
}
