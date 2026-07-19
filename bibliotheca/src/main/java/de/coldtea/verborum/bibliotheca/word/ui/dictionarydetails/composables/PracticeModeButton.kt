package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.ui.components.IconOnTopButton
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.core.theme.VerborumColors
import de.coldtea.verborum.core.theme.VerborumTheme

/**
 * A disabled button is dimmed but deliberately stays tappable: tapping it routes to
 * [onUnavailableClick] so the user is told *why* the mode is unavailable rather than pressing
 * something inert.
 */
@Composable
fun PracticeModeButton(
    modifier: Modifier = Modifier,
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    enabled: Boolean = true,
    onUnavailableClick: () -> Unit = {},
    onClick: () -> Unit
) {
    IconOnTopButton (
        modifier = modifier,
        text = text,
        iconRes = iconRes,
        // Matches the disabled buttons elsewhere in the app (e.g. "Next Question" in the test),
        // which use onSurfaceVariant as their disabled container with white content.
        backgroundColor = if (enabled) backgroundColor else MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { if (enabled) onClick() else onUnavailableClick() },
    )
}


@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun PreviewPracticeModeButton() {
    VerborumTheme {
        PracticeModeButton(
            text = "Self Practice",
            iconRes = ResDrawables.ic_chevron_right_24,
            backgroundColor = VerborumColors.LightAccent
        ) { }
    }
}