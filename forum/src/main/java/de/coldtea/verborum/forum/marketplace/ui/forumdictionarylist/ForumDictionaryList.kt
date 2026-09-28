package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.components.DictionaryCardSkeleton
import de.coldtea.verborum.core.ui.components.ScreenError
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryCard
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.ForumDictionaryOptionsSheet
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryListState
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

/** The Forum tab root: the marketplace's shared dictionaries, laid out like bibliotheca's list. */
@Composable
fun ForumDictionaryListScreen(
    viewModel: ForumDictionaryListViewModel = hiltViewModel(),
    onDictionaryClick: (String) -> Unit,
) {
    val state by viewModel.dictionariesState.collectAsState()

    RegisterTopBar(
        title = stringResource(ResStrings.forumListScreenTitle),
        subtitle = stringResource(ResStrings.forumListScreenSubtitle),
        showBackButton = false,
    )

    ForumDictionaryListContent(
        state = state,
        onDictionaryClick = onDictionaryClick,
        onRetry = viewModel::retry,
    )
}

@Composable
private fun ForumDictionaryListContent(
    state: ForumDictionaryListState,
    onDictionaryClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    // Hoisted so the scroll position survives the Loading -> Success switch.
    val listState = rememberLazyListState()
    var optionsFor by remember { mutableStateOf<ForumDictionaryUi?>(null) }

    optionsFor?.let {
        ForumDictionaryOptionsSheet(
            onDismiss = { optionsFor = null },
            // Download has no action yet — the sheet just closes.
            onDownload = { optionsFor = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is ForumDictionaryListState.Loading -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = 4) { DictionaryCardSkeleton() }
                }
            }

            is ForumDictionaryListState.Failed -> {
                ScreenError(
                    onRetry = onRetry,
                    modifier = Modifier.weight(1f),
                    message = stringResource(ResStrings.forumListLoadError),
                )
            }

            is ForumDictionaryListState.Success -> {
                if (state.dictionaries.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(ResStrings.forumListEmpty),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        // Un-clipped room for the first/last cards' shadow and press-lift.
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.dictionaries,
                            key = { it.dictionaryId },
                        ) { dictionary ->
                            ForumDictionaryCard(
                                dictionary = dictionary,
                                onClick = onDictionaryClick,
                                onMenuClick = { optionsFor = it },
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ForumDictionaryListContentPreview() {
    VerborumTheme {
        ForumDictionaryListContent(
            state = ForumDictionaryListState.Success(
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
            ),
            onDictionaryClick = {},
            onRetry = {},
        )
    }
}
