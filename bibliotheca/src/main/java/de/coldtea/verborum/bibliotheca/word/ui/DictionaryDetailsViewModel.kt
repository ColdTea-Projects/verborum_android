package de.coldtea.verborum.bibliotheca.word.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.model.DictionaryState
import de.coldtea.verborum.bibliotheca.word.ui.model.WordState
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryDetailsViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _dictionaryState = MutableStateFlow<DictionaryState>(DictionaryState.Loading)
    val dictionaryState = _dictionaryState.asSharedFlow()

    private val _wordsState = MutableStateFlow<WordState>(WordState.Loading)
    val wordsState = _wordsState.asSharedFlow()

    fun init(dictionaryId: String) = viewModelScope.launch {
        try {
            _dictionaryState.emit(
                DictionaryState.Success(dictionaryService.getDictionary(dictionaryId))
            )
        } catch (_: Exception) {
            _dictionaryState.emit(DictionaryState.Failed)
        }


        if (_dictionaryState.value !is DictionaryState.Success || (_dictionaryState.value as DictionaryState.Success).dictionaryUi.dictionaryId != dictionaryId) {
            return@launch
        }

        try {
            wordService.observeWordsByDictionary(dictionaryId).observe(
                onSuccess = { _wordsState.emit(WordState.Success(it)) }
            )
        } catch (_: Exception) {
            _wordsState.emit(WordState.Failed)
        }
    }

    fun addDummyDictionary() = viewModelScope.launch {
        if (_dictionaryState.value !is DictionaryState.Success) {
            return@launch
        }

        wordService.addDummyDictionary((_dictionaryState.value as DictionaryState.Success).dictionaryUi.dictionaryId)
    }

    fun cleanWords() = viewModelScope.launch {
        if (_dictionaryState.value !is DictionaryState.Success) {
            return@launch
        }

        wordService.cleanWordsInDictionary((_dictionaryState.value as DictionaryState.Success).dictionaryUi.dictionaryId)
    }
}