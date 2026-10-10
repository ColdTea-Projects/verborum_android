package de.coldtea.verborum.forum.marketplace.domain.usecase.local

import de.coldtea.verborum.core.library.domain.ImportedDictionariesStore
import javax.inject.Inject

/** Hands a just-imported dictionary to bibliotheca, which keeps the local, read-only copy. */
class SaveDownloadedDictionaryUseCase @Inject constructor(
    private val importedDictionariesStore: ImportedDictionariesStore,
) {
    suspend operator fun invoke(dictionaryId: String, publisherName: String?) =
        importedDictionariesStore.onDictionaryImported(dictionaryId, publisherName)
}
