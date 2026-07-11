package de.coldtea.verborum.bibliotheca.word.ui.createword

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.CreateWordState
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.composeWordMeta
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.composeWordText
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWordViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _createWordState = MutableStateFlow<CreateWordState>(CreateWordState.Loading)
    val createWordState = _createWordState.asSharedFlow()

    fun init(dictionaryId: String, wordId: String? = null) {
        dictionaryService.observeDictionary(dictionaryId).observe(
            onSuccess = { dictionary ->
                val editingWord = wordId?.let { wordService.getWord(it) }
                _createWordState.emit(CreateWordState.Success(dictionary, editingWord))
            },
            onError = {
                _createWordState.emit(CreateWordState.Failed)
            }
        )
    }

    fun saveWord(
        wordType: WordType,
        sourceInput: WordFormInput,
        targetInput: WordFormInput,
    ) = viewModelScope.launch(exceptionHandler) {
        val state = _createWordState.value as? CreateWordState.Success ?: return@launch
        val dictionary = state.dictionaryUi
        val editingWord = state.editingWord

        // In edit mode the existing id routes SaveWordUseCase to an update; progress level and
        // creation time carry over from the edited word.
        val word = Word(
            wordId = editingWord?.wordId.orEmpty(),
            dictionaryId = dictionary.dictionaryId,
            word = composeWordText(dictionary.fromLang, sourceInput),
            wordMeta = composeWordMeta(dictionary.fromLang, wordType, sourceInput),
            translation = composeWordText(dictionary.toLang, targetInput),
            translationMeta = composeWordMeta(dictionary.toLang, wordType, targetInput),
            isSynced = false,
            level = editingWord?.level ?: 0,
            createdAt = editingWord?.createdAt ?: getNowInMillis(),
            updatedAt = getNowInMillis(),
        )

        wordService.saveWord(word)
    }
}
