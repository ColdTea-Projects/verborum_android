package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SyncDictionaryTagsUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionariesByUserUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionarySyncedUseCase
import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.MarkWordSyncedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Pushes the signed-in user's local changes and reconciles the local state on
 * success: tombstoned rows (`isDeleted = true`) are deleted remotely then hard-deleted locally;
 * unsynced rows (`isSynced = false`) are uploaded then marked synced. Must run before
 * [SyncUserDictionariesUseCase] so a subsequent download cannot drop or resurrect local data.
 *
 * Success is recorded with a targeted flag update keyed on the uploaded `updatedAt`, never by
 * writing the snapshot back: the row is read before network I/O, so re-saving it whole would
 * revert anything the user changed meanwhile. A row edited mid-flight simply stays unsynced and
 * goes up on the next run.
 */
class UploadPendingChangesUseCase @Inject constructor(
    private val getActiveUserUseCase: GetActiveUserUseCase,
    private val getDictionariesByUserUseCase: GetDictionariesByUserUseCase,
    private val getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase,
    private val saveDictionaryApiUseCase: SaveDictionaryApiUseCase,
    private val syncDictionaryTagsUseCase: SyncDictionaryTagsUseCase,
    private val saveWordApiUseCase: SaveWordApiUseCase,
    private val markDictionarySyncedUseCase: MarkDictionarySyncedUseCase,
    private val markWordSyncedUseCase: MarkWordSyncedUseCase,
    private val deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase,
    private val deleteWordApiUseCase: DeleteWordApiUseCase,
    private val deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
) {

    suspend fun invoke() = withContext(Dispatchers.IO) {
        // Nothing may go up while signed out: guest rows carry GUEST_USER_ID, and that UUID must
        // never reach the server (guide §9.7). Their turn comes after login, once
        // MigrateGuestDataUseCase has re-owned them under the real subject.
        val activeUser = getActiveUserUseCase.invoke() ?: return@withContext

        // Scoped to the active user for the same reason: a row still owned by anyone else — a
        // guest dictionary a failed migration left behind — is not this session's to upload.
        val (deletedDictionaries, activeDictionaries) =
            getDictionariesByUserUseCase.invoke(activeUser).partition { it.isDeleted }

        deletedDictionaries.forEach { uploadDictionaryDeletion(it) }

        activeDictionaries
            .filterNot { it.isSynced }
            .forEach { dictionary ->
                // The dictionary is only marked synced once both its payload and its tags land, so a
                // failed tag reconcile retries next run instead of the download clobbering local tags.
                val dictionaryUploaded = saveDictionaryApiUseCase.invoke(dictionary).isSuccessful
                if (dictionaryUploaded &&
                    syncDictionaryTagsUseCase.push(dictionary.dictionaryId, dictionary.tags)
                ) {
                    markDictionarySyncedUseCase.invoke(
                        dictionaryId = dictionary.dictionaryId,
                        updatedAt = dictionary.updatedAt,
                    )
                }
            }

        activeDictionaries.forEach { dictionary ->
            val (deletedWords, activeWords) =
                getWordsByDictionaryUseCase.invoke(dictionary.dictionaryId)
                    .partition { it.isDeleted }

            deletedWords.forEach { word ->
                if (deleteWordApiUseCase.invoke(word.wordId).isSuccessful) {
                    deleteWordUseCase.invoke(word.wordId)
                }
            }

            activeWords
                .filterNot { it.isSynced }
                .forEach { word ->
                    if (saveWordApiUseCase.invoke(word).isSuccessful) {
                        markWordSyncedUseCase.invoke(
                            wordId = word.wordId,
                            updatedAt = word.updatedAt,
                        )
                    }
                }
        }
    }

    private suspend fun uploadDictionaryDeletion(dictionary: Dictionary) {
        val wordsDeleted =
            deleteWordByDictionaryIdApiUseCase.invoke(dictionary.dictionaryId).isSuccessful
        val dictionaryDeleted =
            deleteDictionaryApiUseCase.invoke(dictionary.dictionaryId).isSuccessful

        if (wordsDeleted && dictionaryDeleted) {
            deleteDictionaryUseCase.invoke(dictionary.dictionaryId)
        }
    }
}
