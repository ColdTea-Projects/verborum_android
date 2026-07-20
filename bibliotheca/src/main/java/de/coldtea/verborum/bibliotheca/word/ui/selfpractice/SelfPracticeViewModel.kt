package de.coldtea.verborum.bibliotheca.word.ui.selfpractice

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model.SelfPracticeState
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelfPracticeViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {
    private val _selfPracticeState =
        MutableStateFlow<SelfPracticeState>(SelfPracticeState.Loading)
    val selfPracticeState = _selfPracticeState.asSharedFlow()

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        observeWords()
    }

    /** Re-subscribes after a Failed load (the observed flow terminates on error). */
    fun retry() {
        _selfPracticeState.tryEmit(SelfPracticeState.Loading)
        observeWords()
    }

    private fun observeWords() {
        loadJob?.cancel()
        loadJob = combine(
            dictionaryService.observeDictionary(dictionaryId).filterNotNull(),
            wordService.observeWordsByDictionary(dictionaryId)
        ) { dictionary, words ->
            SelfPracticeState.Success(dictionary.name, words)
        }.observe(
            onSuccess = { state ->
                _selfPracticeState.emit(state)
            },
            onError = {
                _selfPracticeState.emit(SelfPracticeState.Failed)
            }
        )
    }

    /** Persisting practice progress is a mutation: a failure warns via snackbar but keeps the
     *  session intact rather than replacing it with an error screen. */
    fun onProgressUpdated(wordId: String, progress: Int) = viewModelScope.launch {
        val state = _selfPracticeState.value as? SelfPracticeState.Success ?: return@launch
        val wordUi = state.wordsUi.firstOrNull { it.wordId == wordId } ?: return@launch

        try {
            wordService.saveWord(wordUi.copy(level = progress).convertToWord())
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorSaveFailed))
        }
    }
}