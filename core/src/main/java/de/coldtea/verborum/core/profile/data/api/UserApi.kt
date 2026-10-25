package de.coldtea.verborum.core.profile.data.api

import de.coldtea.verborum.core.profile.data.api.model.ProfileInfoRequest
import de.coldtea.verborum.core.profile.data.api.model.UserProfileRequest
import de.coldtea.verborum.core.profile.data.api.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

/**
 * ms_user (`:8086`) — the profile service. Every call requires the bearer token, and the `/me`
 * calls take no user id at all: the server resolves the caller from the token's `sub`
 * (`docs/marketplace-client-guide.md` §3 rule 2, §4).
 */
interface UserApi {
    /** The caller's own profile; **404** means none exists yet (guide §4.2). */
    @GET("users/me")
    suspend fun getMe(): Response<UserProfileResponse>

    /** Creates the profile — only ever after `/me` answered 404 (guide §4.3). */
    @POST("users/")
    suspend fun createProfile(@Body body: UserProfileRequest): Response<Unit>

    /** Partial update: an omitted field is left unchanged (guide §4.4). */
    @PUT("users/me/profile-info")
    suspend fun updateProfileInfo(@Body body: ProfileInfoRequest): Response<Unit>

    /**
     * Deletes the account and, through events, the data of every service (`docs/account-deletion-guide.md`).
     * 403 `ReauthenticationRequiredException` when the login is older than 5 minutes.
     */
    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>
}
