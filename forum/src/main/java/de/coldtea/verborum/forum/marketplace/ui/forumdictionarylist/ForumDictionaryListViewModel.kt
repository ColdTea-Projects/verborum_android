package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    marketplaceService: MarketplaceService,
) : BaseViewModel() {

    // Set by the search screen (see [applyFilter]); held here so it survives navigating away.
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

    /** The search screen's result; an unchanged filter does not reload. */
    fun applyFilter(filter: ForumDictionaryFilter) {
        _filter.value = filter
    }

    fun clearFilters() {
        _filter.value = ForumDictionaryFilter()
    }
}
