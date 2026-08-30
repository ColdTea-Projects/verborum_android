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
        // Capture the guest-owned ids *before* re-owning them: afterwards they look exactly like
        // dictionaries the subject already had, and re-flagging those would re-upload the user's
        // whole corpus word by word — stale local rows overwriting newer server copies.
        val migratedDictionaryIds = dictionaryRepository
            .getAllDictionariesByUser(GUEST_USER_ID)
            .map { it.dictionaryId }
        if (migratedDictionaryIds.isEmpty()) return@withContext

        dictionaryRepository.reassignOwner(GUEST_USER_ID, subject)
        wordRepository.markWordsUnsyncedInDictionaries(migratedDictionaryIds)
    }
}
