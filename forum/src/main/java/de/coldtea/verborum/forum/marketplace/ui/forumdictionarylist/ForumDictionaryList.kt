package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import de.coldtea.verborum.core.ui.LocalSnackbarHostState
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.VerborumTopBarAction
import de.coldtea.verborum.core.ui.components.DictionaryCardSkeleton
import de.coldtea.verborum.core.ui.components.ScreenError
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryCard
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryOptionsSheet
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumSearchPanel
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.flow.flowOf

/** The Forum tab root: the marketplace's shared dictionaries, laid out like bibliotheca's list. */
@Composable
fun ForumDictionaryListScreen(
    viewModel: ForumDictionaryListViewModel = hiltViewModel(),
    onDictionaryClick: (String) -> Unit,
) {
    val dictionaries = viewModel.dictionaries.collectAsLazyPagingItems()
    val searchExpanded by viewModel.searchExpanded.collectAsState()
    val userNameInputVisible by viewModel.userNameInputVisible.collectAsState()
    val filter by viewModel.filter.collectAsState()

    RegisterTopBar(
        title = stringResource(ResStrings.forumListScreenTitle),
        subtitle = stringResource(ResStrings.forumListScreenSubtitle),
        showBackButton = false,
        // Magnifier on the right toggles the search panel, as on the bibliotheca list.
        action = VerborumTopBarAction(
            iconRes = CoreResDrawables.ic_search_24,
            contentDescription = stringResource(CoreResStrings.dictionaryListSearch),
            onClick = viewModel::toggleSearch,
        ),
    )

    ForumDictionaryListContent(
        dictionaries = dictionaries,
        filter = filter,
        onDictionaryClick = onDictionaryClick,
        searchPanel = {
            // The whole panel expands and collapses together, toggled by the top-bar magnifier.
            // Explicitly vertical: outside a ColumnScope the default transition also grows the
            // width, which slides the chips in from the start edge (bibliotheca's panel does not).
            AnimatedVisibility(
                visible = searchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ForumSearchPanel(
                    filter = filter,
                    userNameInputVisible = userNameInputVisible,
                    onFromFilterChange = viewModel::onFromFilterChange,
                    onToFilterChange = viewModel::onToFilterChange,
                    onToggleTag = viewModel::onToggleTag,
                    onUserClick = viewModel::toggleUserNameInput,
                    onUserNameSearch = viewModel::onUserNameSearch,
                    onClearClick = viewModel::clearFilters,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumDictionaryListContent(
    dictionaries: LazyPagingItems<ForumDictionaryUi>,
    filter: ForumDictionaryFilter,
    onDictionaryClick: (String) -> Unit,
    searchPanel: @Composable () -> Unit = {},
) {
    // Hoisted so the scroll position survives the skeleton -> list switch and back-navigation;
    // keyed by the filter so new search results start from the top.
    val listState = rememberSaveable(filter, saver = LazyListState.Saver) { LazyListState() }
    var optionsFor by remember { mutableStateOf<ForumDictionaryUi?>(null) }

    optionsFor?.let {
        ForumDictionaryOptionsSheet(
            onDismiss = { optionsFor = null },
            // Download has no action yet — the sheet just closes.
            onDownload = { optionsFor = null },
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

            searchPanel()

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
