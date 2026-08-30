package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import javax.inject.Inject

/**
 * Records a successful upload by flipping only the synced flag, leaving every other column alone —
 * the word-side counterpart to
 * [de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionarySyncedUseCase].
 *
 * [updatedAt] is the version that was uploaded: a word edited while the request was in flight no
 * longer matches and stays unsynced, so the edit is neither reverted nor wrongly marked synced.
 */
class MarkWordSyncedUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String, updatedAt: Long) =
        wordRepository.markWordSynced(wordId, updatedAt)
}
