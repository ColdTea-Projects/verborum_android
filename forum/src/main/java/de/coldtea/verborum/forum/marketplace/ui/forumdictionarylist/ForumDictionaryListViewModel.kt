package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    marketplaceService: MarketplaceService,
) : BaseViewModel() {

    // Search panel state. Held here (not in the composable) so it survives navigating to a
    // dictionary and back.
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded = _searchExpanded.asStateFlow()

    private val _userNameInputVisible = MutableStateFlow(false)
    val userNameInputVisible = _userNameInputVisible.asStateFlow()

    private val _filter = MutableStateFlow(ForumDictionaryFilter())
    val filter = _filter.asStateFlow()

    /**
     * The paged browse list for the current [filter]. Every filter change starts a new pager from
     * page 0 (flatMapLatest drops the previous one); loading/error/end state travels inside the
     * paging stream, so the screen reads it from `LazyPagingItems.loadState`. Cached in
     * [viewModelScope] so loaded pages survive configuration changes and back-navigation.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val dictionaries: Flow<PagingData<ForumDictionaryUi>> =
        _filter
            .flatMapLatest { filter -> marketplaceService.getDictionaries(filter) }
            .cachedIn(viewModelScope)

    fun toggleSearch() = _searchExpanded.update { !it }

    fun onFromFilterChange(language: SupportedLanguage?) = _filter.update { it.copy(fromLanguage = language) }

    fun onToFilterChange(language: SupportedLanguage?) = _filter.update { it.copy(toLanguage = language) }

    fun onToggleTag(code: String) = _filter.update { filter ->
        filter.copy(tags = if (code in filter.tags) filter.tags - code else filter.tags + code)
    }

    fun toggleUserNameInput() = _userNameInputVisible.update { !it }

    /**
     * Applies the typed user name — a blank one removes the user filter — and closes the input;
     * the User chip then shows the applied name.
     */
    fun onUserNameSearch(userName: String) {
        _filter.update { it.copy(userName = userName.trim().ifBlank { null }) }
        _userNameInputVisible.value = false
    }

    fun clearFilters() {
        _filter.value = ForumDictionaryFilter()
        _userNameInputVisible.value = false
    }
}
