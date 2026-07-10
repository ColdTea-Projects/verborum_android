package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpsertWordsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Pushes every local change the server has not seen yet (`isSynced = false`) and marks it synced
 * on success. Must run before [SyncUserDictionariesUseCase] so a subsequent download cannot drop
 * local-only data.
 */
class UploadPendingChangesUseCase @Inject constructor(
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
    private val getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase,
    private val saveDictionaryApiUseCase: SaveDictionaryApiUseCase,
    private val saveWordApiUseCase: SaveWordApiUseCase,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val upsertWordsUseCase: UpsertWordsUseCase,
) {

    suspend fun invoke() = withContext(Dispatchers.IO) {
        val dictionaries = getAllDictionariesUseCase.invoke()

        dictionaries
            .filterNot { it.isSynced }
            .forEach { dictionary ->
                if (saveDictionaryApiUseCase.invoke(dictionary).isSuccessful) {
                    saveDictionaryUseCase.invoke(dictionary.copy(isSynced = true))
                }
            }

        dictionaries.forEach { dictionary ->
            getWordsByDictionaryUseCase.invoke(dictionary.dictionaryId)
                .filterNot { it.isSynced }
                .forEach { word ->
                    if (saveWordApiUseCase.invoke(word).isSuccessful) {
                        upsertWordsUseCase.invoke(listOf(word.copy(isSynced = true)))
                    }
                }
        }
    }
}
