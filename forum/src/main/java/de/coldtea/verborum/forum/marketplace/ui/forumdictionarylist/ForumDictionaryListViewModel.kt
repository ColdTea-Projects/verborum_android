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
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryTab
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import androidx.paging.filter
import androidx.paging.map
import de.coldtea.verborum.forum.marketplace.ui.model.ForumDownloads
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    private val marketplaceService: MarketplaceService,
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

    // Null until the default filter (the user's own language pairs) is known, so the lists do not
    // load unfiltered first. Then set by the search screen (see [applyFilter]); held here so it
    // survives navigating away. Both tabs share it.
    private val selectedFilter = MutableStateFlow<ForumDictionaryFilter?>(null)
    val filter: StateFlow<ForumDictionaryFilter> =
        selectedFilter
            .map { it ?: ForumDictionaryFilter() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ForumDictionaryFilter())

    private val _selectedTab = MutableStateFlow(ForumDictionaryTab.POPULAR)
    val selectedTab: StateFlow<ForumDictionaryTab> = _selectedTab.asStateFlow()

    private val downloads = ForumDownloads(marketplaceService)

    private val popularDictionaries = decorated(pagedList(ForumDictionaryTab.POPULAR))
    private val freshDictionaries = decorated(pagedList(ForumDictionaryTab.FRESH))

    init {
        viewModelScope.launch(exceptionHandler) {
            val default = try {
                marketplaceService.getDefaultFilter()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ForumDictionaryFilter()
            }
            // A filter the user already applied wins over the late default.
            selectedFilter.compareAndSet(null, default)
        }
    }

    /** The paged list of [tab] for the current [filter]. */
    fun dictionaries(tab: ForumDictionaryTab): Flow<PagingData<ForumDictionaryUi>> = when (tab) {
        ForumDictionaryTab.POPULAR -> popularDictionaries
        ForumDictionaryTab.FRESH -> freshDictionaries
    }

    fun selectTab(tab: ForumDictionaryTab) {
        _selectedTab.value = tab
    }

    /** Downloads [dictionary] into the user's Bibliotheca; the outcome is reported on the snackbar. */
    fun download(dictionary: ForumDictionaryUi) {
        viewModelScope.launch(exceptionHandler) {
            downloads.download(dictionary, onNotMember = ::refreshAccess)
                ?.let { _snackbarMessages.emit(it) }
        }
    }

    /**
     * Marks each listing as the user's own / downloaded / downloading, and drops one that turned
     * out to be gone. Applied after [cachedIn][androidx.paging.cachedIn], so a download changes
     * the badges without reloading a page.
     */
    private fun decorated(
        pages: Flow<PagingData<ForumDictionaryUi>>,
    ): Flow<PagingData<ForumDictionaryUi>> =
        combine(pages, downloads.userState) { pagingData, userState ->
            pagingData
                .filter { it.dictionaryId !in userState.unavailableIds }
                .map(userState::applyTo)
        }

    /**
     * One tab's paged list. Every filter change starts a new pager from page 0 (flatMapLatest drops
     * the previous one); loading/error/end state travels inside the paging stream, so the screen
     * reads it from `LazyPagingItems.loadState`. Cached in [viewModelScope] so loaded pages
     * survive tab switches, configuration changes and back-navigation. A pager only runs while
     * its tab is on screen.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pagedList(tab: ForumDictionaryTab): Flow<PagingData<ForumDictionaryUi>> =
        selectedFilter
            .filterNotNull()
            .flatMapLatest { filter -> marketplaceService.getDictionaries(filter, tab.order) }
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
        selectedFilter.value = filter
    }

    /** Clears every criterion, the default language pairs included. */
    fun clearFilters() {
        selectedFilter.value = ForumDictionaryFilter()
    }
}
