package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Post-logout wipe: deletes every local word and dictionary so the next account signing in on this
 * device can never see, upload, or reconcile away the previous user's rows (guide §6).
 *
 * Synced rows come back on the next login's full sync; unsynced local edits and pending tombstones
 * are intentionally dropped — losing an offline edit is the accepted price of never mixing
 * owner-keyed data across accounts. Idempotent: a second run deletes nothing.
 */
class ClearLocalDataUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
) {
    suspend fun invoke() = withContext(Dispatchers.IO) {
        wordRepository.deleteAllWords()
        dictionaryRepository.deleteAllDictionaries()
    }
}
