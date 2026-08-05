package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryListState
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionarySort
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
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

    // Search / filter / sort state. Held here (not in the composable) so it survives navigating in
    // and out of the screen and so filtering can be applied and tested in one place.
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded = _searchExpanded.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // null = "Any language".
    private val _fromFilter = MutableStateFlow<SupportedLanguage?>(null)
    val fromFilter = _fromFilter.asStateFlow()

    private val _toFilter = MutableStateFlow<SupportedLanguage?>(null)
    val toFilter = _toFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(DictionarySort.NEWEST)
    val sortOrder = _sortOrder.asStateFlow()

    init {
        observeDictionaries()
        viewModelScope.launch {
            syncService.syncDictionaries()
        }
    }

    private fun observeDictionaries() {
        val dictionariesWithCounts = combine(
            dictionaryService.observeDictionaries(),
            wordService.observeWordCounts(),
        ) { dictionaries, wordCounts ->
            dictionaries.map { it.copy(wordCount = wordCounts[it.dictionaryId] ?: 0) }
        }

        // Re-runs whenever the data or any filter/sort changes, so filtering is immediate.
        combine(
            dictionariesWithCounts,
            _searchQuery,
            _fromFilter,
            _toFilter,
            _sortOrder,
        ) { dictionaries, query, from, to, sort ->
            dictionaries.filterAndSort(query, from, to, sort)
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

    /** Magnifier toggles the search field; collapsing clears the query so its filter lifts. */
    fun toggleSearch() {
        val expanded = !_searchExpanded.value
        _searchExpanded.value = expanded
        if (!expanded) _searchQuery.value = ""
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onFromFilterChange(language: SupportedLanguage?) { _fromFilter.value = language }

    fun onToFilterChange(language: SupportedLanguage?) { _toFilter.value = language }

    fun onSortChange(sort: DictionarySort) { _sortOrder.value = sort }

    /** Resets search, both language filters, and the sort back to defaults (the Clear chip). */
    fun clearFilters() {
        _searchQuery.value = ""
        _fromFilter.value = null
        _toFilter.value = null
        _sortOrder.value = DictionarySort.NEWEST
        _searchExpanded.value = false
    }

    private fun List<DictionaryUi>.filterAndSort(
        query: String,
        from: SupportedLanguage?,
        to: SupportedLanguage?,
        sort: DictionarySort,
    ): List<DictionaryUi> {
        val trimmed = query.trim()
        val filtered = filter { dictionary ->
            (trimmed.isEmpty() || dictionary.name.contains(trimmed, ignoreCase = true)) &&
                (from == null || dictionary.fromLang.equals(from.code, ignoreCase = true)) &&
                (to == null || dictionary.toLang.equals(to.code, ignoreCase = true))
        }
        // A stable id tiebreaker keeps equal rows in a fixed order (no reshuffle on re-emit).
        val comparator: Comparator<DictionaryUi> = when (sort) {
            DictionarySort.NAME_ASC ->
                compareBy({ it.name.lowercase() }, { it.dictionaryId })
            DictionarySort.NAME_DESC ->
                compareByDescending<DictionaryUi> { it.name.lowercase() }.thenBy { it.dictionaryId }
            DictionarySort.FROM_LANGUAGE ->
                compareBy({ it.fromLang }, { it.name.lowercase() }, { it.dictionaryId })
            DictionarySort.TO_LANGUAGE ->
                compareBy({ it.toLang }, { it.name.lowercase() }, { it.dictionaryId })
            DictionarySort.NEWEST ->
                compareByDescending<DictionaryUi> { it.createdAt }.thenBy { it.dictionaryId }
            DictionarySort.OLDEST ->
                compareBy({ it.createdAt }, { it.dictionaryId })
        }
        return filtered.sortedWith(comparator)
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
