package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    marketplaceService: MarketplaceService,
) : BaseViewModel() {

    /**
     * The paged browse list. Loading/error/end state travels inside the paging stream, so the
     * screen reads it from `LazyPagingItems.loadState` and retries through the items themselves.
     * Cached in [viewModelScope] so loaded pages survive configuration changes and back-navigation.
     */
    val dictionaries: Flow<PagingData<ForumDictionaryUi>> =
        marketplaceService.getDictionaries().cachedIn(viewModelScope)
}
