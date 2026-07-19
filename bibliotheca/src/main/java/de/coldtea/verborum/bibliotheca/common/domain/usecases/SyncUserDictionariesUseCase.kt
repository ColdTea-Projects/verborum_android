package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpsertWordsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Merges the server state into the local database without destroying local-only data:
 * - remote rows are upserted, except where the local copy has unsynced changes or a deletion
 *   tombstone (local wins until [UploadPendingChangesUseCase] pushes it),
 * - local rows are deleted only when they are synced yet absent remotely (deleted on the server),
 * - tombstoned local rows absent remotely are hard-deleted (the server no longer has them),
 * - a null API response means "no information" and leaves the local state untouched.
 */
class SyncUserDictionariesUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
    private val wordApi: WordApi,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase,
    private val upsertWordsUseCase: UpsertWordsUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
    //TODO: getActiveUserUseCase
) {

    suspend fun invoke() = withContext(Dispatchers.IO) {
        val activeUser = GUEST_USER_ID // TODO: getActiveUserUseCase.invoke()
        val remoteDictionaries = dictionaryApi.getAllDictionariesByUser(activeUser)
            ?: return@withContext

        val localDictionaries = getAllDictionariesUseCase.invoke()
        val (tombstonedDictionaries, activeDictionaries) =
            localDictionaries.partition { it.isDeleted }
        val localDictionaryById = localDictionaries.associateBy { it.dictionaryId }
        val remoteDictionaryIds = remoteDictionaries.map { it.dictionaryId }.toSet()
        val tombstonedDictionaryIds = tombstonedDictionaries.map { it.dictionaryId }.toSet()
        val locallyModifiedDictionaryIds = activeDictionaries
            .filterNot { it.isSynced }
            .map { it.dictionaryId }
            .toSet()

        activeDictionaries
            .filter { it.isSynced && it.dictionaryId !in remoteDictionaryIds }
            .forEach { deleteDictionaryUseCase.invoke(it.dictionaryId) }

        // The server no longer has these either — the tombstone has served its purpose.
        tombstonedDictionaries
            .filter { it.dictionaryId !in remoteDictionaryIds }
            .forEach { deleteDictionaryUseCase.invoke(it.dictionaryId) }

        remoteDictionaries.forEach { dictionaryResponse ->
            // A pending local deletion must not be resurrected — not even its words.
            if (dictionaryResponse.dictionaryId in tombstonedDictionaryIds) return@forEach

            if (dictionaryResponse.dictionaryId !in locallyModifiedDictionaryIds) {
                val existing = localDictionaryById[dictionaryResponse.dictionaryId]
                val now = getNowInMillis()
                saveDictionaryUseCase.invoke(
                    dictionaryResponse.convertToDictionary(
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = existing?.updatedAt ?: now,
                    )
                )
            }
            syncWords(dictionaryResponse.dictionaryId)
        }
    }

    private suspend fun syncWords(dictionaryId: String) {
        val localWords = getWordsByDictionaryUseCase.invoke(dictionaryId)
        val localWordById = localWords.associateBy { it.wordId }

        val remoteWords = wordApi.getWordsByDictionary(dictionaryId)
            ?.map { response ->
                val existing = localWordById[response.wordId.orEmpty()]
                val now = getNowInMillis()
                response.convertToWord(
                    dictionaryId = dictionaryId,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = existing?.updatedAt ?: now,
                )
            }
            ?: return

        val (tombstonedWords, activeWords) = localWords.partition { it.isDeleted }
        val remoteWordIds = remoteWords.map { it.wordId }.toSet()
        val skippedWordIds = tombstonedWords.map { it.wordId }.toSet() +
            activeWords.filterNot { it.isSynced }.map { it.wordId }

        activeWords
            .filter { it.isSynced && it.wordId !in remoteWordIds }
            .forEach { deleteWordUseCase.invoke(it.wordId) }

        tombstonedWords
            .filter { it.wordId !in remoteWordIds }
            .forEach { deleteWordUseCase.invoke(it.wordId) }

        val wordsToUpsert = remoteWords.filterNot { it.wordId in skippedWordIds }
        if (wordsToUpsert.isNotEmpty()) {
            upsertWordsUseCase.invoke(wordsToUpsert)
        }
    }
}
