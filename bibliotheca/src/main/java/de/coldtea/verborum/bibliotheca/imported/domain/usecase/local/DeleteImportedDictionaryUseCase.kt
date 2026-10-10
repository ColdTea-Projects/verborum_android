package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import javax.inject.Inject

class DeleteImportedDictionaryUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    suspend fun invoke(dictionaryId: String) = repository.deleteDictionary(dictionaryId)
}
