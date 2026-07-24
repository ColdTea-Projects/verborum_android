package de.coldtea.verborum.bibliotheca.auth.data.api

import de.coldtea.verborum.bibliotheca.auth.data.api.model.UserProfileRequest
import de.coldtea.verborum.bibliotheca.auth.data.api.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** ms_user (`:8086`) — the profile service. Every call requires the bearer token. */
interface UserApi {
    @GET("users/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<UserProfileResponse>

    @POST("users/")
    suspend fun createProfile(@Body body: UserProfileRequest): Response<Unit>
}
