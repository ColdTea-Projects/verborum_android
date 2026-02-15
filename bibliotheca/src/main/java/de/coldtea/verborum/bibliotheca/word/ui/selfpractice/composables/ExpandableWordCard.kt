package de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.theme.VerborumColors
import de.coldtea.verborum.core.theme.VerborumTheme
import kotlin.math.absoluteValue

@Composable
fun ExpandableWordCard(
    modifier: Modifier = Modifier,
    word: WordUi,
    isRevealed: Boolean,
    isReversed: Boolean,
    onToggleReveal: () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val threshold = 100f // Pixels to drag before revealing

    val borderColor by animateColorAsState(
        targetValue = if (dragOffset.absoluteValue > threshold / 2 || isRevealed) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(200)
    )

    val (shownText, hiddenText) = if (!isReversed) { word.word to word.translation} else { word.translation to word.word}

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset.absoluteValue > threshold) {
                            onToggleReveal()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, borderColor),
        onClick = onToggleReveal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .padding(20.dp)
        ) {
            // Word (always visible)
            Text(
                text = shownText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )

            // Translation (expands downward with divider)
            androidx.compose.animation.AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                ) + shrinkVertically(
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = hiddenText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewExpandableWordCard() {
    VerborumTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerborumColors.DarkBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Hidden State",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VerborumColors.DarkText
            )
            ExpandableWordCard(
                word = WordUi(
                    wordId = "",
                    dictionaryId = "",
                    word = "Salve",
                    wordMeta = "",
                    translation = "Hello",
                    translationMeta = "",
                    level = 0,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                isRevealed = false,
                isReversed = false,
                onToggleReveal = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Revealed State",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VerborumColors.DarkText
            )
            ExpandableWordCard(
                word = WordUi(
                    wordId = "",
                    dictionaryId = "",
                    word = "Salve",
                    wordMeta = "",
                    translation = "Hello",
                    translationMeta = "",
                    level = 0,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                isRevealed = true,
                isReversed = false,
                onToggleReveal = {}
            )
        }
    }
}