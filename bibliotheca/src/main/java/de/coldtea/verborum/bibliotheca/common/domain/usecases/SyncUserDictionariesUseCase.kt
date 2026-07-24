package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
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
    private val getActiveUserUseCase: GetActiveUserUseCase,
) {

    suspend fun invoke() = withContext(Dispatchers.IO) {
        // No signed-in user means nothing to reconcile against the server (guide §9.7).
        val activeUser = getActiveUserUseCase.invoke() ?: return@withContext
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
                val merged = dictionaryResponse.convertToDictionary(
                    fallbackCreatedAt = existing?.createdAt ?: now,
                    fallbackUpdatedAt = existing?.updatedAt ?: now,
                )
                // Only write when something actually changed: DaoBase.insert is REPLACE, which
                // deletes and re-inserts the row, invalidating Room's observers for no reason.
                if (merged != existing) {
                    saveDictionaryUseCase.invoke(merged)
                }
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
                    fallbackCreatedAt = existing?.createdAt ?: now,
                    fallbackUpdatedAt = existing?.updatedAt ?: now,
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

        // Same reasoning as the dictionary write above: a word already identical locally is
        // dropped from the batch so the upsert cannot churn rows that did not change.
        val wordsToUpsert = remoteWords.filterNot { word ->
            word.wordId in skippedWordIds || word == localWordById[word.wordId]
        }
        if (wordsToUpsert.isNotEmpty()) {
            upsertWordsUseCase.invoke(wordsToUpsert)
        }
    }
}
