package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryListViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
    private val syncService: SyncService,
) : BaseViewModel() {

    private val _dictionariesState = MutableStateFlow(listOf<DictionaryUi>())
    val dictionariesState = _dictionariesState.asStateFlow()

    init {
        viewModelScope.launch {
            dictionaryService.observeDictionaries().observe(
                onSuccess = { dictionary ->
                    _dictionariesState.emit(dictionary)
                },
                onError = {
                    _snackbarMessages.emit("Dictionaries could not be loaded")
                }
            )
             syncService.syncDictionaries()
        }
    }

    fun deleteDictionary(dictionaryId: String) = viewModelScope.launch(exceptionHandler) {
        // Tombstone first: the dictionary disappears immediately and survives offline —
        // the server delete below may fail and will then be retried by the sync upload phase.
        dictionaryService.markDictionaryDeleted(dictionaryId)
        wordService.cleanWordsInDictionary(dictionaryId)
        dictionaryService.deleteDictionary(dictionaryId)
    }
}