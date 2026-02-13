package de.coldtea.verborum.bibliotheca.word.ui.selfpractice

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables.ExpandableWordCard
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables.SwitchWordsButton
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model.SelfPracticeState
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun SelfPracticeScreen(
    viewModel: SelfPracticeViewModel = hiltViewModel()
) {
    val selfPracticeState =
        viewModel.selfPracticeState.collectAsState(initial = SelfPracticeState.Loading).value

    val revealedStates = remember { mutableStateMapOf<String, Boolean>() }
    val reverseMode = remember { mutableStateOf<Boolean>(false) }

    if (selfPracticeState is SelfPracticeState.Success) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Header
                        Text(
                            text = selfPracticeState.dictionaryName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = stringResource(ResStrings.selfPracticeScreenSubtitle),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    SwitchWordsButton {
                        reverseMode.value = !reverseMode.value
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Word Cards
            itemsIndexed(selfPracticeState.wordsUi) { index, word ->
                val isRevealed = revealedStates[word.wordId] ?: false

                ExpandableWordCard(
                    word = word,
                    isRevealed = isRevealed,
                    isReversed = reverseMode.value,
                    onToggleReveal = {
                        revealedStates[word.wordId] = !isRevealed
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun SelfPracticeScreenPreview() {
    VerborumTheme {
        SelfPracticeScreen()
    }
}