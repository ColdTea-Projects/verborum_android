package de.coldtea.verborum.bibliotheca.word.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
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

    suspend fun saveWord(word: Word){
        saveWordUseCase.invoke(word)
    }

    suspend fun cleanWordsInDictionary(dictionaryId: String) {
        deleteWordByDictionaryIdApiUseCase.invoke(dictionaryId)
        deleteWordByDictionaryIdUseCase.invoke(dictionaryId)//TODO: replace with update diff delete
    }

}