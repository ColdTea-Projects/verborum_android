package de.coldtea.verborum.core.profile.data.api

import de.coldtea.verborum.core.profile.data.api.model.VaultEntryResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ms_user's vault — the dictionaries the user imported (downloaded) from the Forum
 * (`docs/marketplace-client-guide.md` §5.5). `{userId}` is ms_user's id from `/users/me`, never
 * the token's `sub`. Entries are added by the marketplace's import, never directly.
 */
interface VaultApi {
    @GET("users/{userId}/vault")
    suspend fun getVault(@Path("userId") userId: String): List<VaultEntryResponse>

    /** Unsubscribe: removes the reference (200, also when it was already gone). */
    @DELETE("users/{userId}/vault/{dictionaryId}")
    suspend fun removeFromVault(
        @Path("userId") userId: String,
        @Path("dictionaryId") dictionaryId: String,
    ): Response<Unit>
}
