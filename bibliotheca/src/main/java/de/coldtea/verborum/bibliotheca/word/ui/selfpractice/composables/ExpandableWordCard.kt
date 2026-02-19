package de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onToggleReveal: () -> Unit,
    onProgressChange: (String, Int) -> Unit,
) {
    val progress = remember { mutableStateOf(word.level) }
    var dragOffset by remember { mutableStateOf(0f) }
    val maxOffset = 50f // Approximately 1/6th of card width (~58dp)
    val threshold = maxOffset * 0.6f // 60% of max offset to trigger progress change

    val borderColor by animateColorAsState(
        targetValue = if (isRevealed) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(100)
    )

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = if (dragOffset == 0f) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        } else {
            tween(500) // No animation during drag
        }
    )

    val (shownText, hiddenText) = if (!isReversed) {
        word.word to word.translation
    } else {
        word.translation to word.word
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = animatedOffset.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset.absoluteValue > threshold) {
                            if (dragOffset.absoluteValue > threshold * 1.5) {
                                // Large swipe - update progress
                                val newProgress = if (dragOffset > 0) {
                                    // Swiped right - increase progress (user knows the word)
                                    minOf(7, progress.value + 1)
                                } else {
                                    // Swiped left - decrease progress (user needs practice)
                                    maxOf(0, progress.value - 1)
                                }
                                progress.value = newProgress
                                onProgressChange(word.wordId, newProgress)
                            } else {
                                // Small swipe - just toggle reveal
                                onToggleReveal()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        // Constrain drag to maxOffset in both directions
                        val newOffset = dragOffset + dragAmount
                        dragOffset = maxOf(-maxOffset, minOf(maxOffset, newOffset))
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, borderColor),
        onClick = onToggleReveal
    ) {
        Box {
            // Progress bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value / 7f)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

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
                    .padding(top = 8.dp) // Account for progress bar
            ) {
                // Word (always visible)
                Text(
                    text = shownText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.3.sp,
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
                    level = 6,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                isRevealed = false,
                isReversed = false,
                onToggleReveal = {},
                onProgressChange = { _, _ -> },
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
                onToggleReveal = {},
                onProgressChange = { _, _ -> },
            )
        }
    }
}