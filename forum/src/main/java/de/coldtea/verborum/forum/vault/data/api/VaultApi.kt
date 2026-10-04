package de.coldtea.verborum.forum.vault.data.api

import de.coldtea.verborum.forum.vault.data.api.model.VaultEntryResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ms_user's vault — the dictionaries the user imported from the Forum
 * (`docs/marketplace-client-guide.md` §5.5). `{userId}` is ms_user's id from `/users/me`, never
 * the token's `sub`.
 */
interface VaultApi {
    @GET("users/{userId}/vault")
    suspend fun getVault(@Path("userId") userId: String): List<VaultEntryResponse>
}
