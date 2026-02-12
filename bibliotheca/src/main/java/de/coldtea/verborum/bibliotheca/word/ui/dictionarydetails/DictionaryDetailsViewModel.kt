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
            DictionaryDetailState.Success(dictionary, words)
        }.observe (
            onSuccess = { state ->
                _dictionaryDetailState.emit(state)
            },
            onError = {
                _dictionaryDetailState.emit(DictionaryDetailState.Failed)
            }
        )
    }

    fun addDummyDictionary() = viewModelScope.launch {
        if (_dictionaryDetailState.value !is DictionaryDetailState.Success) {
            return@launch
        }

        wordService.addDummyDictionary((_dictionaryDetailState.value as DictionaryDetailState.Success).dictionaryUi.dictionaryId)
    }

    fun cleanWords() = viewModelScope.launch {
        if (_dictionaryDetailState.value !is DictionaryDetailState.Success) {
            return@launch
        }

        wordService.cleanWordsInDictionary((_dictionaryDetailState.value as DictionaryDetailState.Success).dictionaryUi.dictionaryId)
    }
}