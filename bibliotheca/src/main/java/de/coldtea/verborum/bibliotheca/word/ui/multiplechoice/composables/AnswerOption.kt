package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun AnswerOption(
    modifier: Modifier = Modifier,
    letter: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(200)
    )

    val offsetX by animateDpAsState(
        targetValue = if (isHovered || isSelected) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = offsetX)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Answer text
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewAnswerOption_notSelected() {
    VerborumTheme {
        AnswerOption(
            letter = "A",
            text = "Answer",
            isSelected = false,
        ) { }
    }
}

@PreviewLightDark
@Composable
fun PreviewAnswerOption_selected() {
    VerborumTheme {
        AnswerOption(
            letter = "B",
            text = "Answer",
            isSelected = true,
        ) { }
    }
}