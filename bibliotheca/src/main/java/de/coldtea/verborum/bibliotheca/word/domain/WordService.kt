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
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
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
            .map { it.map(Word::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    suspend fun addDummyDictionary(dictionaryId: String) {
        val randomPair = generateRandomPair()
        val word = Word(
            wordId = "",
            dictionaryId = dictionaryId,
            word = randomPair.first,
            wordMeta = "{en}",
            translation = randomPair.second,
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

    fun generateRandomPair(len: Int = 15): Pair<String, String> =
        listOfWords.shuffled().first()

    companion object {
        private val listOfWords = listOf(
            "apple" to "der Apfel",
            "travel" to "reisen",
            "on" to "auf",
            "bread" to "das Brot",
            "and" to "und",
            "coffee" to "der Kaffee",
            "tea" to "der Tea",
            "jump" to "springen",
            "now" to "jetzt",
            "later" to "später",
            "mother" to "die Mutter",
            "father" to "der Vater",
            "sister" to "die Schwester",
        )
    }
}