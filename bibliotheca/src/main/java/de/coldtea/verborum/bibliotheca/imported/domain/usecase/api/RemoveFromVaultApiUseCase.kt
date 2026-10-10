package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.core.profile.data.api.VaultApi
import retrofit2.HttpException
import javax.inject.Inject

/** Unsubscribe on the server. Throws unless ms_user confirmed it (200, also when already gone). */
class RemoveFromVaultApiUseCase @Inject constructor(
    private val vaultApi: VaultApi,
) {
    suspend fun invoke(userId: String, dictionaryId: String) {
        val response = vaultApi.removeFromVault(userId = userId, dictionaryId = dictionaryId)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
