package de.coldtea.verborum.bibliotheca.word.ui.selfpractice

import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model.SelfPracticeState
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class SelfPracticeViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _selfPracticeState =
        MutableStateFlow<SelfPracticeState>(SelfPracticeState.Loading)
    val selfPracticeState = _selfPracticeState.asSharedFlow()

    fun init(dictionaryId: String){
        combine(
            dictionaryService.observeDictionary(dictionaryId),
            wordService.observeWordsByDictionary(dictionaryId)
        ) { dictionary, words ->
            SelfPracticeState.Success(dictionary.name, words)
        }.observe (
            onSuccess = { state ->
                _selfPracticeState.emit(state)
            },
            onError = {
                _selfPracticeState.emit(SelfPracticeState.Failed)
            }
        )
    }
}