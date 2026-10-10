package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveImportedWordCountsUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(): Flow<Map<String, Int>> =
        repository.observeWordCounts().map { counts -> counts.associate { it.dictionaryId to it.count } }
}
