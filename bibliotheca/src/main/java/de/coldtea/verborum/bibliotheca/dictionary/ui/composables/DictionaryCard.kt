package de.coldtea.verborum.bibliotheca.dictionary.ui.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResPlurals
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.core.theme.VerborumTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DictionaryCard(
    modifier: Modifier = Modifier,
    dictionary: DictionaryUi,
    index: Int,
    onClick: (String) -> Unit,
    onLongClick: (DictionaryUi) -> Unit = {},
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) (-4).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = animatedOffset)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(), // Material 3 ripple
                onClick = {
                    isPressed = !isPressed
                    onClick(dictionary.dictionaryId)
                },
                onLongClick = { onLongClick(dictionary) },
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Box {
            // Gold accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
            )

            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(ResDrawables.ic_book_24_black),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Dictionary Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dictionary.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = pluralStringResource(
                                ResPlurals.dictionaryListScreenWordCount,
                                dictionary.wordCount,
                                dictionary.wordCount,
                            ),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = relativeTimeAgo(dictionary.createdAt),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chevron
                Icon(
                    painter = painterResource(ResDrawables.ic_chevron_right_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// "3 days ago", "5 minutes ago", … localized by the platform; recomputed only when createdAt changes.
@Composable
private fun relativeTimeAgo(createdAt: Long): String =
    remember(createdAt) {
        DateUtils.getRelativeTimeSpanString(
            createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }

@PreviewLightDark
@Composable
fun PreviewDictionaryCardLight() {
    VerborumTheme {
        DictionaryCard(
            dictionary = DictionaryUi(
                dictionaryId = "dictionaryId",
                userId = "userId",
                name = "Sample Dictionary",
                isPublic = false,
                fromLang = "EN",
                toLang = "DE",
                createdAt = System.currentTimeMillis() - 3 * DateUtils.DAY_IN_MILLIS,
                updatedAt = 0L,
                wordCount = 12,
            ),
            index = 0,
            onClick = {},
        )
    }
}