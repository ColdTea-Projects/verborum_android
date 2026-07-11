package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Live word count per dictionary id, keyed for quick lookup by the dictionary list. */
class ObserveWordCountsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    fun invoke(): Flow<Map<String, Int>> =
        wordRepository.observeWordCounts()
            .map { counts -> counts.associate { it.dictionaryId to it.count } }
}
