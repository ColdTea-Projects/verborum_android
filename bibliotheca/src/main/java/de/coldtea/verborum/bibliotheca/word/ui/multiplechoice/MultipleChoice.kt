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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables.QuestionCard
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState

@Composable
fun MultipleChoiceQuestionScreen(
    viewModel: MultipleChoiceViewModel = hiltViewModel()
) {
    val currentQuestionState = viewModel.currentQuestion.collectAsState(MultipleChoiceCurrentQuestionState.Loading).value
    val feedback = viewModel.feedback.collectAsState("").value

    when(currentQuestionState){
        is MultipleChoiceCurrentQuestionState.Success -> {
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
                    progress = (currentQuestionState.index / currentQuestionState.size).toFloat(),
                    onAnswerSelected = viewModel::onAnswerReceived
                )


                Spacer(modifier = Modifier.weight(1f))

                // Next Button (if answer selected)
                if (!feedback.isEmpty()) {
                    Button(
                        onClick = viewModel::onNextQuestionRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Next Question",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        else -> {}
    }
}