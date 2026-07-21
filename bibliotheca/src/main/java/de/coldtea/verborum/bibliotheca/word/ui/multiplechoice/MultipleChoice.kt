package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.components.ScreenError
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.MultipleChoiceContent
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.ResultScreen
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages

@Composable
fun MultipleChoiceQuestionScreen(
    viewModel: MultipleChoiceViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val currentQuestionState =
        viewModel.currentQuestion.collectAsState(MultipleChoiceCurrentQuestionState.Loading).value
    val answered = viewModel.answered.collectAsState(false).value
    val selectedAnswer = viewModel.selectedAnswer.collectAsState("").value

    // Only errors (e.g. a failed level save) use the snackbar now; the correct/incorrect result
    // is shown inline by MultipleChoiceContent so it never blocks the action buttons.
    ShowSnackbarMessages(viewModel.snackbarMessages)

    when (currentQuestionState) {
        is MultipleChoiceCurrentQuestionState.Success -> {
            RegisterTopBar(
                title = stringResource(ResStrings.testScreenTitle),
                subtitle = stringResource(
                    ResStrings.testQuestionProgress,
                    currentQuestionState.index,
                    currentQuestionState.size,
                ),
                showBackButton = true,
            )
            MultipleChoiceContent(
                currentQuestionState = currentQuestionState,
                answered = answered,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = viewModel::onAnswerReceived,
                onNextQuestionRequested = viewModel::onNextQuestionRequested,
                onAnswerGiven = viewModel::onAnswerGiven,
            )
        }

        is MultipleChoiceCurrentQuestionState.Completed -> {
            RegisterTopBar(
                title = stringResource(ResStrings.testCompleteTitle),
                showBackButton = true,
            )
            ResultScreen(
                resultState = currentQuestionState,
                onBackClick = onBackClick,
                onRetryClick = viewModel::onRetryClicked,
            )
        }

        is MultipleChoiceCurrentQuestionState.Failed -> {
            RegisterTopBar(title = stringResource(ResStrings.testScreenTitle), showBackButton = true)
            ScreenError(onRetry = viewModel::retry)
        }

        is MultipleChoiceCurrentQuestionState.NotEnoughWords -> {
            RegisterTopBar(title = stringResource(ResStrings.testScreenTitle), showBackButton = true)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(ResStrings.dictionaryDetailsScreenNotEnoughWordsForTest),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        is MultipleChoiceCurrentQuestionState.Loading -> Unit
    }
}
