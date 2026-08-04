package de.coldtea.verborum.bibliotheca.word.ui.createword

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.CreateWordState
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.composeWordMeta
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.composeWordText
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWordViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _createWordState = MutableStateFlow<CreateWordState>(CreateWordState.Loading)
    val createWordState = _createWordState.asSharedFlow()

    // One-shot signal that a save succeeded (payload = was it an edit), so the screen navigates
    // back on edit / clears the form on create — only then, never on a failed save.
    private val _wordSaved = MutableSharedFlow<Boolean>()
    val wordSaved = _wordSaved.asSharedFlow()

    private var loadJob: Job? = null
    private var dictionaryId: String = ""
    private var wordId: String? = null

    // Read once and held, so the word-count emissions below don't re-read it — and, more to the
    // point, don't hand the screen a fresh instance that would re-run its prefill mid-edit.
    private var editingWord: WordUi? = null

    fun init(dictionaryId: String, wordId: String? = null) {
        this.dictionaryId = dictionaryId
        this.wordId = wordId
        observeDictionary()
    }

    /** Re-subscribes after a Failed load (the observed flow terminates on error). */
    fun retry() {
        _createWordState.tryEmit(CreateWordState.Loading)
        // The cached word may be what failed to load; fetch it again.
        editingWord = null
        observeDictionary()
    }

    private fun observeDictionary() {
        loadJob?.cancel()
        // The word count rides along with the dictionary so the header reflects a save the moment
        // the row lands, without the screen having to ask for it.
        loadJob = combine(
            dictionaryService.observeDictionary(dictionaryId).filterNotNull(),
            wordService.observeWordCounts(),
        ) { dictionary, wordCounts ->
            dictionary.copy(wordCount = wordCounts[dictionary.dictionaryId] ?: 0)
        }.observe(
            onSuccess = { dictionary ->
                try {
                    val word = editingWord ?: wordId?.let { wordService.getWord(it) }
                        ?.also { editingWord = it }
                    _createWordState.emit(CreateWordState.Success(dictionary, word))
                } catch (e: Exception) {
                    _createWordState.emit(CreateWordState.Failed)
                }
            },
            onError = {
                _createWordState.emit(CreateWordState.Failed)
            }
        )
    }

    fun saveWord(
        wordType: WordType,
        sourceInputs: List<WordFormInput>,
        targetInputs: List<WordFormInput>,
    ) = viewModelScope.launch(exceptionHandler) {
        val state = _createWordState.value as? CreateWordState.Success ?: return@launch
        val dictionary = state.dictionaryUi
        val editingWord = state.editingWord

        // Drop blank alternatives so the surface list and meta arrays stay index-aligned; keep at
        // least the first so a half-filled word still saves.
        val sources = sourceInputs.filter { it.text.isNotBlank() }.ifEmpty { sourceInputs.take(1) }
        val targets = targetInputs.filter { it.text.isNotBlank() }.ifEmpty { targetInputs.take(1) }

        // In edit mode the existing id routes SaveWordUseCase to an update; progress level and
        // creation time carry over from the edited word.
        val word = Word(
            wordId = editingWord?.wordId.orEmpty(),
            dictionaryId = dictionary.dictionaryId,
            word = composeWordText(dictionary.fromLang, sources),
            wordMeta = composeWordMeta(dictionary.fromLang, wordType, sources),
            translation = composeWordText(dictionary.toLang, targets),
            translationMeta = composeWordMeta(dictionary.toLang, wordType, targets),
            isSynced = false,
            level = editingWord?.level ?: 0,
            createdAt = editingWord?.createdAt ?: getNowInMillis(),
            updatedAt = getNowInMillis(),
        )

        try {
            wordService.saveWord(word)
            _wordSaved.emit(editingWord != null)
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorSaveFailed))
        }
    }
}
