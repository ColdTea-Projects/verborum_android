package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryDetailsViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _dictionaryDetailState =
        MutableStateFlow<DictionaryDetailState>(DictionaryDetailState.Loading)
    val dictionaryDetailState = _dictionaryDetailState.asSharedFlow()

    fun init(dictionaryId: String) = viewModelScope.launch {
        combine(
            dictionaryService.observeDictionary(dictionaryId),
            wordService.observeWordsByDictionary(dictionaryId)
        ) { dictionary, words ->
            // A null dictionary means it was tombstoned/removed — surface it as Deleted so the
            // screen navigates back instead of re-rendering (and re-registering) a stale header.
            if (dictionary == null) DictionaryDetailState.Deleted
            else DictionaryDetailState.Success(dictionary, words)
        }.observe (
            onSuccess = { state ->
                _dictionaryDetailState.emit(state)
            },
            onError = {
                _dictionaryDetailState.emit(DictionaryDetailState.Failed)
            }
        )
    }

    fun deleteWord(wordId: String) = viewModelScope.launch(exceptionHandler) {
        wordService.deleteWord(wordId)
    }

    /**
     * Tombstones the dictionary first so it disappears immediately and offline-safely, then cleans
     * its words and performs the server-confirmed delete. A failed network call leaves the
     * tombstone for the sync upload phase to retry.
     */
    fun deleteDictionary() = viewModelScope.launch(exceptionHandler) {
        val state = _dictionaryDetailState.value as? DictionaryDetailState.Success ?: return@launch
        val dictionaryId = state.dictionaryUi.dictionaryId

        dictionaryService.markDictionaryDeleted(dictionaryId)
        wordService.cleanWordsInDictionary(dictionaryId)
        dictionaryService.deleteDictionary(dictionaryId)
    }
}