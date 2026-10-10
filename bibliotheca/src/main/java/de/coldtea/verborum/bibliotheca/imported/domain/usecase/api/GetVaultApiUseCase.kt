package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.core.profile.data.api.VaultApi
import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.bibliotheca.imported.domain.model.VaultEntry
import javax.inject.Inject

/** The ids of every dictionary the user has downloaded, on any device. */
class GetVaultApiUseCase @Inject constructor(
    private val vaultApi: VaultApi,
) {
    suspend fun invoke(userId: String): List<VaultEntry> =
        vaultApi.getVault(userId).map { entry ->
            VaultEntry(dictionaryId = entry.dictionaryId, importedAt = ApiTimestamp.parse(entry.importedAt))
        }
}
