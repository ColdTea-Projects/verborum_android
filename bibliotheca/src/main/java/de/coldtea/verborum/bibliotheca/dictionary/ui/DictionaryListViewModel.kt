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
import kotlinx.coroutines.flow.combine
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
            combine(
                dictionaryService.observeDictionaries(),
                wordService.observeWordCounts(),
            ) { dictionaries, wordCounts ->
                dictionaries.map { it.copy(wordCount = wordCounts[it.dictionaryId] ?: 0) }
            }.observe(
                onSuccess = { dictionaries ->
                    _dictionariesState.emit(dictionaries)
                },
                onError = {
                    _snackbarMessages.emit("Dictionaries could not be loaded")
                }
            )
            syncService.syncDictionaries()
        }
    }

    /**
     * Tombstones the dictionary first so it disappears immediately and offline-safely, then cleans
     * its words and performs the server-confirmed delete — mirrors the details screen's delete.
     */
    fun deleteDictionary(dictionaryId: String) = viewModelScope.launch(exceptionHandler) {
        dictionaryService.markDictionaryDeleted(dictionaryId)
        wordService.cleanWordsInDictionary(dictionaryId)
        dictionaryService.deleteDictionary(dictionaryId)
    }
}
