package de.coldtea.verborum.bibliotheca.word.ui.selfpractice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.components.ScreenError
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables.ExpandableWordCard
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model.SelfPracticeState
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages

@Composable
fun SelfPracticeScreen(
    viewModel: SelfPracticeViewModel = hiltViewModel()
) {
    val selfPracticeState =
        viewModel.selfPracticeState.collectAsState(initial = SelfPracticeState.Loading).value

    ShowSnackbarMessages(viewModel.snackbarMessages)

    val revealedStates = remember { mutableStateMapOf<String, Boolean>() }
    val reverseMode = remember { mutableStateOf(false) }
    val wordIdOrder = remember { mutableStateOf<List<String>>(listOf()) }

    if (selfPracticeState is SelfPracticeState.Failed) {
        RegisterTopBar(title = stringResource(ResStrings.errorScreenTitle), showBackButton = true)
        ScreenError(onRetry = viewModel::retry)
    }

    if (selfPracticeState is SelfPracticeState.Success) {
        if (wordIdOrder.value.isEmpty()) {
            wordIdOrder.value = selfPracticeState.wordsUi.map { it.wordId }.shuffled()
        }

        RegisterTopBar(
            title = selfPracticeState.dictionaryName,
            subtitle = stringResource(ResStrings.selfPracticeScreenSubtitle),
            showBackButton = true,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Word Cards
                items(wordIdOrder.value) { wordId ->
                    val word = selfPracticeState.wordsUi.first { it.wordId == wordId }
                    val isRevealed = revealedStates[word.wordId] ?: false

                    ExpandableWordCard(
                        word = word,
                        isRevealed = isRevealed,
                        isReversed = reverseMode.value,
                        onToggleReveal = {
                            revealedStates[word.wordId] = !isRevealed
                        },
                        onProgressChange = viewModel::onProgressUpdated
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Sticky bottom action: flip which side of each card is shown.
            Button(
                onClick = { reverseMode.value = !reverseMode.value },
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
                Text(
                    text = stringResource(ResStrings.selfPracticeScreenSwitch),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
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
