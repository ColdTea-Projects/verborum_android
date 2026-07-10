package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.CreateDictionaryState
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateDictionaryViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
) : BaseViewModel() {

    private val _createDictionaryState =
        MutableStateFlow<CreateDictionaryState>(CreateDictionaryState.Idle)
    val createDictionaryState = _createDictionaryState.asSharedFlow()

    fun create(name: String, fromLang: SupportedLanguage, toLang: SupportedLanguage) =
        viewModelScope.launch(exceptionHandler) {
            if (_createDictionaryState.value is CreateDictionaryState.Saving) return@launch

            _createDictionaryState.emit(CreateDictionaryState.Saving)

            try {
                val dictionaryId = dictionaryService.createDictionary(
                    name = name.trim(),
                    fromLang = fromLang.code,
                    toLang = toLang.code,
                )
                _createDictionaryState.emit(CreateDictionaryState.Created(dictionaryId))
            } catch (e: Exception) {
                _createDictionaryState.emit(CreateDictionaryState.Failed)
            }
        }
}
