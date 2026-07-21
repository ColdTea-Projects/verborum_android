package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.DeleteDictionaryDialog
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.DictionaryCard
import de.coldtea.verborum.bibliotheca.common.ui.components.ScreenError
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.DictionaryCardSkeleton
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryListState
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryListScreen(
    viewModel: DictionaryListViewModel = hiltViewModel(),
    onDictionaryClick: (String) -> Unit,
    onCreateDictionaryClick: () -> Unit,
    onEditDictionaryClick: (String) -> Unit = {},
) {
    val dictionaryListState = viewModel.dictionariesState.collectAsState().value
    val isRefreshing = viewModel.isRefreshing.collectAsState().value

    // Hoisted so the scroll position survives the Loading -> Success switch and screen
    // navigation, instead of being re-created per state branch.
    val listState = rememberLazyListState()

    // Long-press target for the options sheet, and the dictionary pending delete confirmation.
    var optionsFor by remember { mutableStateOf<DictionaryUi?>(null) }
    var confirmDeleteFor by remember { mutableStateOf<DictionaryUi?>(null) }

    // Delete failures (and any other one-off notice) surface on the shared snackbar.
    ShowSnackbarMessages(viewModel.snackbarMessages)

    optionsFor?.let { dictionary ->
        DictionaryOptionsSheet(
            onDismiss = { optionsFor = null },
            onEdit = {
                optionsFor = null
                onEditDictionaryClick(dictionary.dictionaryId)
            },
            onDelete = {
                optionsFor = null
                confirmDeleteFor = dictionary
            },
        )
    }

    confirmDeleteFor?.let { dictionary ->
        DeleteDictionaryDialog(
            dictionaryName = dictionary.name,
            onConfirm = {
                confirmDeleteFor = null
                viewModel.deleteDictionary(dictionary.dictionaryId)
            },
            onDismiss = { confirmDeleteFor = null },
        )
    }

    RegisterTopBar(
        title = stringResource(ResStrings.dictionaryListScreenHeader),
        subtitle = stringResource(ResStrings.dictionaryListScreenSubtitle),
        showBackButton = false,
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
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

            when (dictionaryListState) {
                is DictionaryListState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(count = 4) {
                            DictionaryCardSkeleton()
                        }
                    }
                }

                is DictionaryListState.Failed -> {
                    ScreenError(
                        onRetry = viewModel::retry,
                        modifier = Modifier.weight(1f),
                        message = stringResource(ResStrings.dictionaryListScreenLoadError),
                    )
                }

                is DictionaryListState.Success -> {
                    // Dictionary List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        // spacedBy only pads between items; without content padding the first and
                        // last cards' shadow and press-lift are clipped by the list bounds and look
                        // cut off. This gives the edges un-clipped breathing room.
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = dictionaryListState.dictionaries,
                            // Stable identity, deliberately with no item animation. The query is
                            // now explicitly ordered, so an unchanged list is literally the same
                            // list and nothing recomposes. When items do arrive, the key lets
                            // LazyColumn anchor the scroll on the item you are looking at, so
                            // rows loading off-screen never shift the visible ones.
                            key = { _, dictionary -> dictionary.dictionaryId },
                        ) { index, dictionary ->
                            DictionaryCard(
                                dictionary = dictionary,
                                index = index,
                                onClick = onDictionaryClick,
                                onLongClick = { optionsFor = it },
                            )
                        }
                    }
                }
            }

            // Sticky bottom action
            Button(
                onClick = onCreateDictionaryClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(ResDrawables.ic_plus_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(ResStrings.dictionaryListScreenCreate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionaryOptionsSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            DictionaryOptionRow(
                iconRes = ResDrawables.ic_edit_24,
                text = stringResource(ResStrings.dictionaryOptionsEdit),
                // Neutral action — reads against the background like normal content.
                color = MaterialTheme.colorScheme.onBackground,
                onClick = onEdit,
            )
            DictionaryOptionRow(
                iconRes = ResDrawables.ic_delete_24,
                text = stringResource(ResStrings.dictionaryOptionsDelete),
                // Destructive action — flagged in the error color.
                color = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun DictionaryOptionRow(
    iconRes: Int,
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    VerborumTheme {
        DictionaryListScreen(
            onDictionaryClick = { _ -> },
            onCreateDictionaryClick = {}
        )
    }
}
