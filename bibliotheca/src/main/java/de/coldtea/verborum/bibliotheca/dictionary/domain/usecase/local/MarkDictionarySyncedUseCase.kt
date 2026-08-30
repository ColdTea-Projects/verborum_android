package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import javax.inject.Inject

/**
 * Records a successful upload by flipping only the synced flag, leaving every other column alone.
 *
 * [updatedAt] is the version that was uploaded: a row edited while the request was in flight no
 * longer matches and stays unsynced, so the newer content uploads on the next run instead of being
 * overwritten by the stale snapshot.
 */
class MarkDictionarySyncedUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend fun invoke(dictionaryId: String, updatedAt: Long) =
        dictionaryRepository.markDictionarySynced(dictionaryId, updatedAt)
}
