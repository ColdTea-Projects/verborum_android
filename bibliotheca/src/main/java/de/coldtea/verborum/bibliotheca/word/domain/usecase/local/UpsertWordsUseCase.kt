package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

/**
 * Inserts or replaces words exactly as given, preserving their `isSynced` flag. Used by the sync
 * flow to store server data — unlike [SaveWordUseCase], which marks every save as a local change.
 */
class UpsertWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(words: List<Word>) =
        wordRepository.saveWords(words.map(Word::convertToEntity))
}
