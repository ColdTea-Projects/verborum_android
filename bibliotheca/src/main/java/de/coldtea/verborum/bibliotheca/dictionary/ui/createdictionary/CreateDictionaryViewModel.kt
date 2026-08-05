package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.CreateDictionaryState
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateDictionaryViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
) : BaseViewModel() {

    private val _createDictionaryState =
        MutableStateFlow<CreateDictionaryState>(CreateDictionaryState.Idle)
    val createDictionaryState = _createDictionaryState.asSharedFlow()

    // Non-null once the screen is opened for an existing dictionary; drives prefill and the update path.
    private val _editingDictionary = MutableStateFlow<DictionaryUi?>(null)
    val editingDictionary = _editingDictionary.asStateFlow()

    /** Loads the dictionary to edit; a null/blank id (create mode) or an already-loaded one is a no-op. */
    fun init(dictionaryId: String?) {
        if (dictionaryId.isNullOrEmpty() || _editingDictionary.value != null) return
        viewModelScope.launch {
            try {
                _editingDictionary.emit(dictionaryService.getDictionary(dictionaryId))
            } catch (e: Exception) {
                // Prefill failed — tell the user; the form stays usable as a blank create form.
                _snackbarMessages.emit(UiText.Resource(ResStrings.errorScreenMessage))
            }
        }
    }

    fun save(
        name: String,
        fromLang: SupportedLanguage,
        toLang: SupportedLanguage,
        tags: List<String>,
    ) =
        viewModelScope.launch(exceptionHandler) {
            if (_createDictionaryState.value is CreateDictionaryState.Saving) return@launch

            _createDictionaryState.emit(CreateDictionaryState.Saving)

            try {
                val editing = _editingDictionary.value
                val result = if (editing == null) {
                    val dictionaryId = dictionaryService.createDictionary(
                        name = name.trim(),
                        fromLang = fromLang.code,
                        toLang = toLang.code,
                        tags = tags,
                    )
                    CreateDictionaryState.Created(dictionaryId)
                } else {
                    dictionaryService.updateDictionary(
                        editing.copy(
                            name = name.trim(),
                            fromLang = fromLang.code,
                            toLang = toLang.code,
                            tags = tags,
                        )
                    )
                    CreateDictionaryState.Updated
                }
                _createDictionaryState.emit(result)
            } catch (e: Exception) {
                _createDictionaryState.emit(CreateDictionaryState.Failed)
                _snackbarMessages.emit(UiText.Resource(ResStrings.errorSaveFailed))
            }
        }
}
