package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.compose.foundation.background
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryTab
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.key
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.LocalIsOnline
import de.coldtea.verborum.core.ui.LocalSnackbarHostState
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages
import de.coldtea.verborum.core.ui.VerborumTopBarAction
import de.coldtea.verborum.core.ui.components.DictionaryCardSkeleton
import de.coldtea.verborum.core.ui.components.ScreenError
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryCard
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryOptionsSheet
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceAccessDeniedException
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumActiveFilters
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumMembershipRequired
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumAccessState
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.flow.flowOf

/**
 * The Forum tab root: the marketplace's shared dictionaries, laid out like bibliotheca's list.
 * Members only (guide §6.3) — anyone else sees how to join instead, and the list is not loaded.
 */
@Composable
fun ForumDictionaryListScreen(
    viewModel: ForumDictionaryListViewModel = hiltViewModel(),
    onDictionaryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val access by viewModel.accessState.collectAsState()

    // Download outcomes surface here.
    ShowSnackbarMessages(viewModel.snackbarMessages)

    // Every visit re-checks membership: the user may have joined or left since the last one.
    LaunchedEffect(Unit) { viewModel.refreshAccess() }

    // Coming back online re-checks membership too, which also retries a check that failed offline.
    val isOnline = LocalIsOnline.current
    var wasOnline by remember { mutableStateOf(isOnline) }
    LaunchedEffect(isOnline) {
        if (isOnline && !wasOnline) viewModel.refreshAccess()
        wasOnline = isOnline
    }

    RegisterTopBar(
        title = stringResource(ResStrings.forumListScreenTitle),
        subtitle = stringResource(ResStrings.forumListScreenSubtitle),
        showBackButton = false,
        // Magnifier on the right opens the search screen, which applies its criteria here.
        action = if (access == ForumAccessState.Member) {
            VerborumTopBarAction(
                iconRes = CoreResDrawables.ic_search_24,
                contentDescription = stringResource(CoreResStrings.dictionaryListSearch),
                onClick = onSearchClick,
            )
        } else {
            null
        },
    )

    when (access) {
        ForumAccessState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        ForumAccessState.NotMember -> ForumMembershipRequired()

        ForumAccessState.Failed -> ScreenError(onRetry = viewModel::refreshAccess)

        ForumAccessState.Member -> ForumDictionaryListMember(
            viewModel = viewModel,
            onDictionaryClick = onDictionaryClick,
            onSearchClick = onSearchClick,
        )
    }
}

/**
 * The member's Forum: the Popular and Fresh tabs over the same filter. Only the selected tab is
 * composed — so only its pager runs — and each tab keeps its own scroll position.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumDictionaryListMember(
    viewModel: ForumDictionaryListViewModel,
    onDictionaryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabStates = rememberSaveableStateHolder()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            ForumDictionaryTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(text = stringResource(tab.labelRes), fontWeight = FontWeight.Medium) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        key(selectedTab) {
            tabStates.SaveableStateProvider(selectedTab.name) {
                ForumDictionaryTabContent(
                    viewModel = viewModel,
                    tab = selectedTab,
                    onDictionaryClick = onDictionaryClick,
                    onSearchClick = onSearchClick,
                )
            }
        }
    }
}

/**
 * One tab's paged list. Offline there is no list at all — the pager is not even collected, so
 * nothing stale is shown (the banner explains why) — and the way back online reloads it from page 0.
 */
@Composable
private fun ForumDictionaryTabContent(
    viewModel: ForumDictionaryListViewModel,
    tab: ForumDictionaryTab,
    onDictionaryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val filter by viewModel.filter.collectAsState()
    val isOnline = LocalIsOnline.current

    // Outlives the online branch below, which leaves composition while offline.
    var wasOffline by remember { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { if (!isOnline) wasOffline = true }

    val activeFilters: @Composable () -> Unit = {
        ForumActiveFilters(
            filter = filter,
            onFilterClick = onSearchClick,
            onClearClick = viewModel::clearFilters,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }

    if (!isOnline) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            activeFilters()
        }
        return
    }

    val dictionaries = viewModel.dictionaries(tab).collectAsLazyPagingItems()

    // The cached pages replay on resubscribing, so a fresh load from page 0 follows a reconnect.
    LaunchedEffect(Unit) {
        if (wasOffline) {
            wasOffline = false
            dictionaries.refresh()
        }
    }

    // Refused as a non-member (left on another device): re-check, so the gate shows how to join.
    val refresh = dictionaries.loadState.refresh
    LaunchedEffect(refresh) {
        if (refresh is LoadState.Error && refresh.error is MarketplaceAccessDeniedException) {
            viewModel.refreshAccess()
        }
    }

    ForumDictionaryListContent(
        dictionaries = dictionaries,
        filter = filter,
        onDictionaryClick = onDictionaryClick,
        onDownload = viewModel::download,
        activeFilters = activeFilters,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumDictionaryListContent(
    dictionaries: LazyPagingItems<ForumDictionaryUi>,
    filter: ForumDictionaryFilter,
    onDictionaryClick: (String) -> Unit,
    onDownload: (ForumDictionaryUi) -> Unit = {},
    activeFilters: @Composable () -> Unit = {},
) {
    // Hoisted so the scroll position survives the skeleton -> list switch and back-navigation;
    // keyed by the filter so new search results start from the top.
    val listState = rememberSaveable(filter, saver = LazyListState.Saver) { LazyListState() }
    var optionsFor by remember { mutableStateOf<ForumDictionaryUi?>(null) }

    optionsFor?.let { dictionary ->
        ForumDictionaryOptionsSheet(
            onDismiss = { optionsFor = null },
            onDownload = {
                optionsFor = null
                onDownload(dictionary)
            },
        )
    }

    val refresh = dictionaries.loadState.refresh
    // Only a refresh over a shown list spins the pull indicator; the first load has its skeleton.
    val isRefreshing = refresh is LoadState.Loading && dictionaries.itemCount > 0

    // A failed pull keeps the old list on screen, so the failure is reported on the snackbar.
    val snackbarHostState = LocalSnackbarHostState.current
    val refreshErrorMessage = stringResource(ResStrings.forumListLoadError)
    LaunchedEffect(refresh) {
        if (refresh is LoadState.Error && dictionaries.itemCount > 0) {
            snackbarHostState.showSnackbar(refreshErrorMessage)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        // A new PagingSource from page 0, so newly published dictionaries appear at the top.
        onRefresh = dictionaries::refresh,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            activeFilters()

            when {
                // Once items exist, a later refresh keeps them on screen instead of blanking the list.
                dictionaries.itemCount > 0 -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        // Un-clipped room for the first/last cards' shadow and press-lift.
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            count = dictionaries.itemCount,
                            key = dictionaries.itemKey { it.dictionaryId },
                            contentType = dictionaries.itemContentType { DICTIONARY_CONTENT_TYPE },
                        ) { index ->
                            // Placeholders are disabled, so a loaded index is never null.
                            dictionaries[index]?.let { dictionary ->
                                ForumDictionaryCard(
                                    dictionary = dictionary,
                                    onClick = onDictionaryClick,
                                    onMenuClick = { optionsFor = it },
                                )
                            }
                        }

                        appendFooter(
                            append = dictionaries.loadState.append,
                            onRetry = dictionaries::retry,
                        )
                    }
                }

                refresh is LoadState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(count = 4) { DictionaryCardSkeleton() }
                    }
                }

                refresh is LoadState.Error -> {
                    ScreenError(
                        onRetry = dictionaries::retry,
                        modifier = Modifier.weight(1f),
                        message = stringResource(ResStrings.forumListLoadError),
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            // Filters that exclude everything read differently from an empty forum.
                            text = stringResource(
                                if (filter.isActive) ResStrings.forumListNoMatches else ResStrings.forumListEmpty
                            ),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** The list's last row while the next page loads, or failed to (with an inline retry). */
private fun LazyListScope.appendFooter(
    append: LoadState,
    onRetry: () -> Unit,
) {
    when (append) {
        is LoadState.Loading -> item(key = FOOTER_KEY, contentType = FOOTER_KEY) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is LoadState.Error -> item(key = FOOTER_KEY, contentType = FOOTER_KEY) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(ResStrings.forumListLoadMoreError),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(text = stringResource(CoreResStrings.errorRetry))
                }
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

private const val DICTIONARY_CONTENT_TYPE = "dictionary"
private const val FOOTER_KEY = "appendFooter"

@PreviewLightDark
@Composable
private fun ForumDictionaryListContentPreview() {
    VerborumTheme {
        ForumDictionaryListContent(
            dictionaries = flowOf(
                PagingData.from(
                    listOf(
                        ForumDictionaryUi(
                            dictionaryId = "1",
                            publisherId = "p1",
                            publisherName = "Anna Schmidt",
                            name = "Everyday German",
                            fromLang = "EN",
                            toLang = "DE",
                            downloadCount = 1284,
                            publishedAt = System.currentTimeMillis(),
                            rating = 4.7f,
                            wordCount = 6,
                        ),
                    )
                )
            ).collectAsLazyPagingItems(),
            filter = ForumDictionaryFilter(),
            onDictionaryClick = {},
        )
    }
}
