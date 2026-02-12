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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables.PracticeModeButton
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables.WordListItem
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun DictionaryDetailsScreen(
    viewModel: DictionaryDetailsViewModel = hiltViewModel(),
    onTestClicked: () -> Unit,
    onSelfPracticeClicked: () -> Unit,
) {
    val dictionaryDetailState =
        viewModel.dictionaryDetailState.collectAsState(initial = DictionaryDetailState.Loading).value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        item {
            Row {
                Button(onClick = viewModel::addDummyDictionary) {
                    Text(text = "Add")
                }
                Button(onClick = viewModel::cleanWords) {
                    Text(text = "Delete")
                }
            }
        }
        if (dictionaryDetailState is DictionaryDetailState.Success) {
            val dictionary = dictionaryDetailState.dictionaryUi
            val words = dictionaryDetailState.wordsUi

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Header
                Text(
                    text = dictionary.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )
                val headerSubtext =
                    if (words.isEmpty()) stringResource(ResStrings.dictionaryDetailsScreenWordListZeroItem)
                    else pluralStringResource(
                        ResPlurals.dictionaryDetailsScreenWordListCount,
                        words.size,
                        words.size,
                    )

                Text(
                    text = headerSubtext,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
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
                            words.forEachIndexed { index, word ->
                                WordListItem(word = word)

                                if (index < words.size - 1) {
                                    Spacer(modifier = Modifier.height(0.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
        )
    }
}