package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveImportedWordsUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(dictionaryId: String): Flow<List<ImportedWord>> =
        repository.observeWords(dictionaryId).map { it.map(ImportedWordEntity::convertToImportedWord) }
}
