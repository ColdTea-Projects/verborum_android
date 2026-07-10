package de.coldtea.verborum.bibliotheca.common.domain.usecases

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
 * - remote rows are upserted, except where the local copy has unsynced changes (local wins until
 *   [UploadPendingChangesUseCase] pushes it),
 * - local rows are deleted only when they are synced yet absent remotely (deleted on the server),
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
        val remoteDictionaryIds = remoteDictionaries.map { it.dictionaryId }.toSet()
        val locallyModifiedDictionaryIds = localDictionaries
            .filterNot { it.isSynced }
            .map { it.dictionaryId }
            .toSet()

        localDictionaries
            .filter { it.isSynced && it.dictionaryId !in remoteDictionaryIds }
            .forEach { deleteDictionaryUseCase.invoke(it.dictionaryId) }

        remoteDictionaries.forEach { dictionaryResponse ->
            if (dictionaryResponse.dictionaryId !in locallyModifiedDictionaryIds) {
                saveDictionaryUseCase.invoke(dictionaryResponse.convertToDictionary())
            }
            syncWords(dictionaryResponse.dictionaryId)
        }
    }

    private suspend fun syncWords(dictionaryId: String) {
        val remoteWords = wordApi.getWordsByDictionary(dictionaryId)
            ?.map { it.convertToWord(dictionaryId) }
            ?: return

        val localWords = getWordsByDictionaryUseCase.invoke(dictionaryId)
        val remoteWordIds = remoteWords.map { it.wordId }.toSet()
        val locallyModifiedWordIds = localWords
            .filterNot { it.isSynced }
            .map { it.wordId }
            .toSet()

        localWords
            .filter { it.isSynced && it.wordId !in remoteWordIds }
            .forEach { deleteWordUseCase.invoke(it.wordId) }

        val wordsToUpsert = remoteWords.filterNot { it.wordId in locallyModifiedWordIds }
        if (wordsToUpsert.isNotEmpty()) {
            upsertWordsUseCase.invoke(wordsToUpsert)
        }
    }
}
