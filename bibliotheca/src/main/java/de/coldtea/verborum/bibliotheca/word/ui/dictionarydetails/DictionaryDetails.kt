package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResPlurals
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.DeleteDictionaryDialog
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.languagePairLabel
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables.PracticeModeButton
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables.WordListItem
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar

@Composable
fun DictionaryDetailsScreen(
    viewModel: DictionaryDetailsViewModel = hiltViewModel(),
    onTestClicked: () -> Unit,
    onSelfPracticeClicked: () -> Unit,
    onCreateWordClicked: () -> Unit,
    onEditWordClicked: (String) -> Unit = {},
    onDictionaryDeleted: () -> Unit = {},
) {
    val dictionaryDetailState =
        viewModel.dictionaryDetailState.collectAsState(initial = DictionaryDetailState.Loading).value

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigation is driven by the observed state, not the delete button: once the dictionary is
    // gone the screen leaves exactly once, so the async delete can't race the back navigation and
    // the torn-down screen can never re-register the shared top bar with the stale dictionary.
    LaunchedEffect(dictionaryDetailState) {
        if (dictionaryDetailState is DictionaryDetailState.Deleted) onDictionaryDeleted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        if (dictionaryDetailState is DictionaryDetailState.Success) {
            val dictionary = dictionaryDetailState.dictionaryUi
            val words = dictionaryDetailState.wordsUi

            val languagePair = languagePairLabel(dictionary.fromLang, dictionary.toLang)
            val wordCount = pluralStringResource(
                ResPlurals.dictionaryListScreenWordCount,
                words.size,
                words.size,
            )

            RegisterTopBar(
                title = dictionary.name,
                subtitle = "$languagePair · $wordCount",
                showBackButton = true,
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Practice Mode Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PracticeModeButton(
                            text = stringResource(ResStrings.dictionaryDetailsScreenTest),
                            iconRes = ResDrawables.ic_check_square_24,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = onTestClicked
                        )

                        PracticeModeButton(
                            text = stringResource(ResStrings.dictionaryDetailsScreenSelf),
                            iconRes = ResDrawables.ic_play_24,
                            backgroundColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = onSelfPracticeClicked
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Word List Section
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(ResStrings.dictionaryDetailsScreenWordListHeader),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                words.forEach { word ->
                                    WordListItem(
                                        word = word,
                                        onEditClick = onEditWordClicked,
                                        onDeleteClick = viewModel::deleteWord,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Sticky bottom actions
            OutlinedButton(
                onClick = onCreateWordClicked,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(ResDrawables.ic_plus_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(ResStrings.dictionaryDetailsScreenCreateWord),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Button(
                onClick = { showDeleteDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp)
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(ResDrawables.ic_delete_24),
                    contentDescription = null,
                    // Inherits the button's error content color to match the red text.
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDictionary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            if (showDeleteDialog) {
                DeleteDictionaryDialog(
                    dictionaryName = dictionary.name,
                    onConfirm = {
                        showDeleteDialog = false
                        viewModel.deleteDictionary()
                    },
                    onDismiss = { showDeleteDialog = false },
                )
            }
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    VerborumTheme {
        DictionaryDetailsScreen(
            onTestClicked = {},
            onSelfPracticeClicked = {},
            onCreateWordClicked = {},
        )
    }
}
