package de.coldtea.verborum.forum.vault.domain.usecase.api

import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.forum.vault.data.VaultRepository
import javax.inject.Inject

/**
 * The languages of the dictionaries the user imported and can still read. The vault only holds
 * ids, so the dictionaries are fetched in one batch; one that became unavailable is skipped.
 */
class GetVaultDictionaryLanguagesApiUseCase @Inject constructor(
    private val repository: VaultRepository,
) {
    suspend operator fun invoke(userId: String): List<DictionaryLanguages> {
        val entries = repository.getVault(userId)
        if (entries.isEmpty()) return emptyList()

        val importedAt = entries.associate { it.dictionaryId to (ApiTimestamp.parse(it.importedAt) ?: 0L) }
        return repository.getDictionaries(importedAt.keys.toList()).map { dictionary ->
            DictionaryLanguages(
                fromLang = dictionary.fromLang,
                toLang = dictionary.toLang,
                lastActivityAt = importedAt[dictionary.dictionaryId] ?: 0L,
            )
        }
    }
}
