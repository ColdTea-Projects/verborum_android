package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.core.extensions.rememberDebounced
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun MultipleChoiceContent(
    modifier: Modifier = Modifier,
    currentQuestionState: MultipleChoiceCurrentQuestionState.Success,
    answered: Boolean,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit,
    onNextQuestionRequested: () -> Unit,
    onAnswerGiven: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Question Card

        QuestionCard(
            question = currentQuestionState.multipleChoiceCurrentQuestion,
            progress = currentQuestionState.index.toFloat() / currentQuestionState.size.toFloat(),
            selectedAnswer = selectedAnswer,
            isActive = !answered,
            onAnswerSelected = onAnswerSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Inline result, shown right above the actions.
        val question = currentQuestionState.multipleChoiceCurrentQuestion.question
        if (answered) {
            AnswerFeedback(
                isCorrect = selectedAnswer == question.answer,
                correctAnswer = question.answer,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Check answer (active until the answer is submitted)
        Button(
            onClick = rememberDebounced(onAnswerGiven),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedAnswer.isNotEmpty() && !answered,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(ResStrings.testCheckAnswer),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Next question (active once the answer has been checked)
        Button(
            onClick = rememberDebounced(onNextQuestionRequested),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = answered,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(ResStrings.testNextQuestion),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Non-blocking answer result shown inline above the action buttons: gold when correct, error red
 * when wrong (mirroring the pass/fail colours of the result screen). Uses the app's theme like
 * every other surface, rather than a system toast, so it reads consistently.
 */
@Composable
private fun AnswerFeedback(
    isCorrect: Boolean,
    correctAnswer: String,
    modifier: Modifier = Modifier,
) {
    val color = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = if (isCorrect) {
                stringResource(ResStrings.testCorrectAnswer)
            } else {
                stringResource(ResStrings.testIncorrectAnswer, correctAnswer)
            },
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@PreviewLightDark
@Composable
fun MultipleChoiceContentPreview() {
    VerborumTheme {
        ResultScreen(
            resultState = MultipleChoiceCurrentQuestionState.Completed(
                true,
                90,
                9,
                10
            ),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}