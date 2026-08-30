package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import javax.inject.Inject

/**
 * Saves practice progress for a single word, leaving every other column untouched.
 *
 * Practice screens hold a snapshot of their words, which can go stale while a session is open —
 * writing a whole word back from that snapshot would revert edits and resurrect deletions, so the
 * level is written on its own. The word is flagged unsynced so the new level uploads.
 */
class UpdateWordLevelUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String, level: Int, updatedAt: Long) =
        wordRepository.updateWordLevel(wordId, level, updatedAt)
}
