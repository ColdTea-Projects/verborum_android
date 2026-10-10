package de.coldtea.verborum.forum.marketplace.domain.usecase.local

import de.coldtea.verborum.core.library.domain.ImportedDictionariesStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** The ids of the dictionaries downloaded to this device (kept by bibliotheca). */
class ObserveDownloadedDictionaryIdsUseCase @Inject constructor(
    private val importedDictionariesStore: ImportedDictionariesStore,
) {
    operator fun invoke(): Flow<Set<String>> = importedDictionariesStore.observeImportedDictionaryIds()
}
