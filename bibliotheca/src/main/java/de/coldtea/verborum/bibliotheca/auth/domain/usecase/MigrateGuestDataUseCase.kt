package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * First-login guest-data migration (guide §6.4/§9.7). Re-owns every guest dictionary under the
 * signed-in subject and re-flags the affected rows unsynced, so the normal upload pushes them to
 * the server under the real owner. Idempotent — a second run finds no guest-owned rows. Runs
 * entirely client-side; the guest UUID must never reach the server.
 */
class MigrateGuestDataUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(subject: String) = withContext(Dispatchers.IO) {
        if (subject == GUEST_USER_ID) return@withContext
        val reassigned = dictionaryRepository.reassignOwner(GUEST_USER_ID, subject)
        if (reassigned > 0) {
            wordRepository.markWordsUnsyncedForUser(subject)
        }
    }
}
