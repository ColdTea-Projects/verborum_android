package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Emits null while the dictionary is not downloaded (or after unsubscribing). */
class ObserveImportedDictionaryUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(dictionaryId: String): Flow<ImportedDictionary?> =
        repository.observeDictionary(dictionaryId).map { it?.convertToImportedDictionary() }
}
