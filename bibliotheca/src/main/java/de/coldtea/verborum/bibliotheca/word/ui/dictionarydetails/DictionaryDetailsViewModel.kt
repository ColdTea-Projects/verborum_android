package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.REQUIRED_WORDS_FOR_TEST
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.Job
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

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        observeDetails()
    }

    /** Re-subscribes after a Failed load (the observed flow terminates on error). */
    fun retry() {
        _dictionaryDetailState.tryEmit(DictionaryDetailState.Loading)
        observeDetails()
    }

    private fun observeDetails() {
        loadJob?.cancel()
        loadJob = combine(
            dictionaryService.observeDictionary(dictionaryId),
            wordService.observeWordsByDictionary(dictionaryId),
            wordService.observeWordsInLanguagePair(dictionaryId),
        ) { dictionary, words, languagePairWords ->
            // A null dictionary means it was tombstoned/removed — surface it as Deleted so the
            // screen navigates back instead of re-rendering (and re-registering) a stale header.
            if (dictionary == null) {
                DictionaryDetailState.Deleted
            } else {
                val distinctInPair =
                    languagePairWords.distinctBy { it.word + it.translation }.size
                DictionaryDetailState.Success(
                    dictionaryUi = dictionary,
                    wordsUi = words,
                    canSelfPractice = words.isNotEmpty(),
                    canTest = words.isNotEmpty() && distinctInPair >= REQUIRED_WORDS_FOR_TEST,
                )
            }
        }.observe (
            onSuccess = { state ->
                _dictionaryDetailState.emit(state)
            },
            onError = {
                _dictionaryDetailState.emit(DictionaryDetailState.Failed)
            }
        )
    }

    fun deleteWord(wordId: String) = viewModelScope.launch {
        try {
            wordService.deleteWord(wordId)
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteFailed))
        }
    }

    /**
     * Tombstones the dictionary first so it disappears immediately and offline-safely, then cleans
     * its words and performs the server-confirmed delete. A failed network call leaves the
     * tombstone for the sync upload phase to retry; only a thrown local write is surfaced.
     */
    fun deleteDictionary() = viewModelScope.launch {
        val state = _dictionaryDetailState.value as? DictionaryDetailState.Success ?: return@launch
        val dictionaryId = state.dictionaryUi.dictionaryId

        try {
            dictionaryService.markDictionaryDeleted(dictionaryId)
            wordService.cleanWordsInDictionary(dictionaryId)
            dictionaryService.deleteDictionary(dictionaryId)
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteFailed))
        }
    }
}