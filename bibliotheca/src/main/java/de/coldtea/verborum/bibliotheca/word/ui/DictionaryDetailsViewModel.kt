package de.coldtea.verborum.bibliotheca.word.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local.CleanDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.ui.model.DictionaryState
import de.coldtea.verborum.bibliotheca.word.ui.model.Failed
import de.coldtea.verborum.bibliotheca.word.ui.model.Loading
import de.coldtea.verborum.bibliotheca.word.ui.model.Success
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryDetailsViewModel @Inject constructor(
    private val dictionaryService: DictionaryService
) : BaseViewModel() {

    private val _dictionariesState = MutableStateFlow<DictionaryState>(Loading)
    val dictionariesState = _dictionariesState.asSharedFlow()

    fun init(dictionaryId: String) = viewModelScope.launch {
        try {
            _dictionariesState.emit(
                Success(dictionaryService.getDictionary(dictionaryId))
            )
        }catch (_: Exception){
            _dictionariesState.emit(Failed)
        }
    }
}