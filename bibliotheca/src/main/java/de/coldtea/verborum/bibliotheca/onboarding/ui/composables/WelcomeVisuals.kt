package de.coldtea.verborum.bibliotheca.onboarding.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.CoreResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.core.theme.VerborumTheme

// The mock cards below illustrate real app screens with fixed sample content
// ("German Basics", "gehen", …) — deliberately not localized, like previews.

/** Page 1: the Verborum book emblem. */
@Composable
fun IntroVisual(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondary,
        shadowElevation = 8.dp,
        modifier = modifier.size(120.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(CoreResDrawables.ic_book_24_black),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

/** Page 2: a miniature dictionary card plus an add-word row. */
@Composable
fun LibraryVisual(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        MockCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(CoreResDrawables.ic_book_24_black),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "German Basics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "English → German",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    painter = painterResource(ResDrawables.ic_chevron_right_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        MockCard(borderColor = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(ResDrawables.ic_plus_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "der Apfel · Äpfel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "apple",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Page 3: a miniature multiple-choice question with the correct answer highlighted. */
@Composable
fun TestVisual(modifier: Modifier = Modifier) {
    MockCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "What does \"go\" mean?",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            MockAnswerRow(letter = "A", text = "kommen", isSelected = false)
            Spacer(modifier = Modifier.height(8.dp))
            MockAnswerRow(letter = "B", text = "gehen", isSelected = true)
            Spacer(modifier = Modifier.height(8.dp))
            MockAnswerRow(letter = "C", text = "laufen", isSelected = false)
        }
    }
}

/** Page 4: a miniature practice card with the swipe directions around it. */
@Composable
fun PracticeVisual(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(ResDrawables.ic_chevron_right_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            // Mirrored chevron: swiping left lowers the progress.
            modifier = Modifier
                .size(28.dp)
                .scale(scaleX = -1f, scaleY = 1f)
        )

        MockCard(modifier = Modifier.weight(1f)) {
            Column {
                // Progress bar like the practice card's top edge.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(4 / 7f)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "gehen · ging · (sein) gegangen",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "go · went · gone",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Icon(
            painter = painterResource(ResDrawables.ic_chevron_right_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun MockCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun MockAnswerRow(letter: String, text: String, isSelected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = letter,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
fun WelcomeVisualsPreview() {
    VerborumTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            IntroVisual()
            LibraryVisual()
            TestVisual()
            PracticeVisual()
        }
    }
}
