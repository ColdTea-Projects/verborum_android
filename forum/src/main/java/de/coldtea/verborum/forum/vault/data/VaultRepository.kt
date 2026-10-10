package de.coldtea.verborum.forum.vault.data

import de.coldtea.verborum.forum.marketplace.data.api.SharedDictionaryApi
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryResponse
import de.coldtea.verborum.core.profile.data.api.VaultApi
import de.coldtea.verborum.core.profile.data.api.model.VaultEntryResponse
import javax.inject.Inject

/** The user's vault and the dictionaries it points at — always read live, nothing stored. */
class VaultRepository @Inject constructor(
    private val vaultApi: VaultApi,
    private val sharedDictionaryApi: SharedDictionaryApi,
) {
    suspend fun getVault(userId: String): List<VaultEntryResponse> = vaultApi.getVault(userId)

    /** Unreadable ids (made private or deleted) are silently left out by the server (guide §6.7). */
    suspend fun getDictionaries(dictionaryIds: List<String>): List<SharedDictionaryResponse> =
        sharedDictionaryApi.getDictionaries(dictionaryIds.joinToString(separator = ","))
}
