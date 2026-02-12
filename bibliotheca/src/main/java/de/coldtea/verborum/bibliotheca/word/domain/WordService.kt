package de.coldtea.verborum.bibliotheca.word.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.WordUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordService @Inject constructor(
    private val observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase,
    private val deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase,
    private val deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase,
    private val saveWordUseCase: SaveWordUseCase,
    private val syncService: SyncService,
    private val uploadService: UploadService,
) {

    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordUi>> =
        observeWordsByDictionaryUseCase
            .invoke(dictionaryId)
            .distinctUntilChanged()
            .map { it.map(Word::convertToUi) }
            .flowOn(Dispatchers.IO)

    suspend fun addDummyDictionary(dictionaryId: String) {
        val word = Word(
            wordId = "",
            dictionaryId = dictionaryId,
            word = generateRandomString(),
            wordMeta = "{en}",
            translation = generateRandomString(),
            translationMeta = "{de}",
            isSynced = false,
            createdAt = getNowInMillis(),
            updatedAt = getNowInMillis(),
        )

        saveWordUseCase.invoke(word)
        try {
            uploadService.createWord(word)
            syncService.syncDictionaries()
        } catch (e: Exception) {
            // Other errors
            Log.e("Sync", "Unexpected error", e)
        }
    }

    suspend fun cleanWordsInDictionary(dictionaryId: String) {
        deleteWordByDictionaryIdApiUseCase.invoke(dictionaryId)
        deleteWordByDictionaryIdUseCase.invoke(dictionaryId)//TODO: replace with update diff delete
    }

    fun generateRandomString(len: Int = 15): String {
        val alphanumerics = CharArray(26) { (it + 97).toChar() }.toSet()
            .union(CharArray(9) { (it + 48).toChar() }.toSet())
        return (0..<len).map {
            alphanumerics.toList().random()
        }.joinToString("")
    }
}