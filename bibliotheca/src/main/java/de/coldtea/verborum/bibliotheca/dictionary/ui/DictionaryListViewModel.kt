package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryListState
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
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

    private val _dictionariesState =
        MutableStateFlow<DictionaryListState>(DictionaryListState.Loading)
    val dictionariesState = _dictionariesState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        observeDictionaries()
        viewModelScope.launch {
            syncService.syncDictionaries()
        }
    }

    private fun observeDictionaries() {
        combine(
            dictionaryService.observeDictionaries(),
            wordService.observeWordCounts(),
        ) { dictionaries, wordCounts ->
            dictionaries.map { it.copy(wordCount = wordCounts[it.dictionaryId] ?: 0) }
        }.observe(
            onSuccess = { dictionaries ->
                _dictionariesState.emit(DictionaryListState.Success(dictionaries))
            },
            onError = {
                _dictionariesState.emit(DictionaryListState.Failed)
            }
        )
    }

    /** Re-subscribes after a Failed state — the observed flow terminates on error. */
    fun retry() {
        _dictionariesState.value = DictionaryListState.Loading
        observeDictionaries()
    }

    /**
     * User-initiated foreground sync for pull-to-refresh. Runs the same [SyncService] the
     * background worker uses; [SyncService] handles its own errors, so the spinner always clears.
     */
    fun refresh() = viewModelScope.launch {
        _isRefreshing.emit(true)
        syncService.syncDictionaries()
        _isRefreshing.emit(false)
    }

    /**
     * Tombstones the dictionary first so it disappears immediately and offline-safely, then cleans
     * its words and performs the server-confirmed delete — mirrors the details screen's delete.
     * A failure here means the local tombstone write itself threw (the API-delete path is
     * offline-safe and simply retries via sync), so it is surfaced rather than swallowed.
     */
    fun deleteDictionary(dictionaryId: String) = viewModelScope.launch {
        try {
            dictionaryService.markDictionaryDeleted(dictionaryId)
            wordService.cleanWordsInDictionary(dictionaryId)
            dictionaryService.deleteDictionary(dictionaryId)
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteFailed))
        }
    }
}
