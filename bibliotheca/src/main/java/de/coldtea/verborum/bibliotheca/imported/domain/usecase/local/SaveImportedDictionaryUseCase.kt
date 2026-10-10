package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionaryContent
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord
import javax.inject.Inject

/** Stores the owner's current version; words still present keep the user's local level. */
class SaveImportedDictionaryUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    suspend fun invoke(content: ImportedDictionaryContent) =
        repository.saveDictionary(
            dictionary = content.dictionary.convertToEntity(),
            words = content.words.map(ImportedWord::convertToEntity),
        )
}
