package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.MultipleChoiceContent
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.QuestionCard
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.ResultScreen
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.core.extensions.debounce

@Composable
fun MultipleChoiceQuestionScreen(
    viewModel: MultipleChoiceViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
) {
    val currentQuestionState =
        viewModel.currentQuestion.collectAsState(MultipleChoiceCurrentQuestionState.Loading).value
    val answered = viewModel.answered.collectAsState(false).value
    val selectedAnswer = viewModel.selectedAnswer.collectAsState("").value
    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    when (currentQuestionState) {
        is MultipleChoiceCurrentQuestionState.Success -> {
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
            ResultScreen(
                resultState = currentQuestionState,
                onBackClick = onBackClick,
                onRetryClick = viewModel::onRetryClicked,
            )
        }

        else -> {}
    }
}
