package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpsertWordsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Pushes every local change the server has not seen yet and reconciles the local state on
 * success: tombstoned rows (`isDeleted = true`) are deleted remotely then hard-deleted locally;
 * unsynced rows (`isSynced = false`) are uploaded then marked synced. Must run before
 * [SyncUserDictionariesUseCase] so a subsequent download cannot drop or resurrect local data.
 */
class UploadPendingChangesUseCase @Inject constructor(
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
    private val getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase,
    private val saveDictionaryApiUseCase: SaveDictionaryApiUseCase,
    private val saveWordApiUseCase: SaveWordApiUseCase,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val upsertWordsUseCase: UpsertWordsUseCase,
    private val deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase,
    private val deleteWordApiUseCase: DeleteWordApiUseCase,
    private val deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
) {

    suspend fun invoke() = withContext(Dispatchers.IO) {
        val (deletedDictionaries, activeDictionaries) =
            getAllDictionariesUseCase.invoke().partition { it.isDeleted }

        deletedDictionaries.forEach { uploadDictionaryDeletion(it) }

        activeDictionaries
            .filterNot { it.isSynced }
            .forEach { dictionary ->
                if (saveDictionaryApiUseCase.invoke(dictionary).isSuccessful) {
                    saveDictionaryUseCase.invoke(dictionary.copy(isSynced = true))
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
                        upsertWordsUseCase.invoke(listOf(word.copy(isSynced = true)))
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
