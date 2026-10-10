package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveImportedDictionariesUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(): Flow<List<ImportedDictionary>> =
        repository.observeDictionaries().map { it.map(ImportedDictionaryEntity::convertToImportedDictionary) }
}
