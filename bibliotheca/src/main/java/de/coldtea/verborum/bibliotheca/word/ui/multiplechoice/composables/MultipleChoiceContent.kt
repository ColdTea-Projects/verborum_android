package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.core.extensions.debounce
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
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Test Mode",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Question ${currentQuestionState.index} of ${currentQuestionState.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question Card

        QuestionCard(
            question = currentQuestionState.multipleChoiceCurrentQuestion,
            progress = currentQuestionState.index.toFloat() / currentQuestionState.size.toFloat(),
            selectedAnswer = selectedAnswer,
            isActive = !answered,
            onAnswerSelected = onAnswerSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        // Next Button (if answer selected)
        Button(
            onClick = debounce(onNextQuestionRequested),
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
                text = "Next Question",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Check answer Button (if answer selected)
        Button(
            onClick = debounce(onAnswerGiven),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !selectedAnswer.isEmpty() && !answered,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Check",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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