package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumAccessState
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    marketplaceService: MarketplaceService,
    private val profileService: ProfileService,
) : BaseViewModel() {

    private val accessLoadFailed = MutableStateFlow(false)

    /**
     * The Forum gate (guide §6.3), driven by the session's profile: it flips as soon as the user
     * joins or leaves on the profile page. A cached profile keeps deciding when a refresh fails;
     * only having none at all is [ForumAccessState.Failed].
     */
    val accessState: StateFlow<ForumAccessState> =
        combine(profileService.observeProfile(), accessLoadFailed) { profile, failed ->
            when {
                profile != null ->
                    if (profile.isMarketplaceMember) ForumAccessState.Member else ForumAccessState.NotMember
                failed -> ForumAccessState.Failed
                else -> ForumAccessState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ForumAccessState.Loading)

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

    /** Re-checks membership with `/users/me` — on every visit, since it can change on another device. */
    fun refreshAccess() {
        accessLoadFailed.value = false
        viewModelScope.launch(exceptionHandler) {
            try {
                profileService.refreshProfile()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                accessLoadFailed.value = true
            }
        }
    }

    /** The search screen's result; an unchanged filter does not reload. */
    fun applyFilter(filter: ForumDictionaryFilter) {
        _filter.value = filter
    }

    fun clearFilters() {
        _filter.value = ForumDictionaryFilter()
    }
}
