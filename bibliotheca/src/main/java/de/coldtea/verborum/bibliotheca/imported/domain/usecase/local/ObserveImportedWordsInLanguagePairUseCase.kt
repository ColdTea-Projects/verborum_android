package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Downloaded words in dictionaries of exactly this direction — part of a test's distractor pool. */
class ObserveImportedWordsInLanguagePairUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(fromLang: String, toLang: String): Flow<List<ImportedWord>> =
        repository.observeWordsInLanguagePair(fromLang, toLang)
            .map { it.map(ImportedWordEntity::convertToImportedWord) }
}
