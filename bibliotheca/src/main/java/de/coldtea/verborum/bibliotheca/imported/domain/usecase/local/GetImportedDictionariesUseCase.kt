package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary
import javax.inject.Inject

class GetImportedDictionariesUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    suspend fun invoke(): List<ImportedDictionary> =
        repository.getDictionaries().map(ImportedDictionaryEntity::convertToImportedDictionary)
}
