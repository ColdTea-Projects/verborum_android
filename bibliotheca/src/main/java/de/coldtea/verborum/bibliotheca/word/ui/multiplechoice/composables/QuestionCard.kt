package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestion
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceQuestion
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun QuestionCard(
    question: MultipleChoiceCurrentQuestion,
    progress: Float,
    selectedAnswer: String,
    isActive: Boolean,
    onAnswerSelected: (String) -> Unit,
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Question Text
            Text(
                text = "What does \"${question.question.question}\" mean?",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Answer Options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                question.choices.forEachIndexed {index, option ->
                    AnswerOption(
                        letter = ('A' + index).toString(),
                        text = option,
                        isSelected = selectedAnswer == option,
                        onClick = {
                            if(isActive) onAnswerSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewQuestionCard_notSelected() {
    VerborumTheme {
        QuestionCard(
            question = MultipleChoiceCurrentQuestion(
                question = MultipleChoiceQuestion("","Question", "Anseer"),
                choices = listOf("Wrong answer 1", "Answer", "Wrong answer 2", "Wrong answer 3"),
            ),
            selectedAnswer = "Answer",
            isActive = true,
            progress = 0.4f,
        ) { }
    }
}