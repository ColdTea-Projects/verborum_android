package de.coldtea.verborum.core.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/** How long the highlight takes to fade from the accent colour back to the subtitle colour. */
private const val HIGHLIGHT_FADE_MILLIS = 2_000

/** Separator between the subtitle and its highlighted segment, as used across the app. */
private const val SEPARATOR = " · "

/**
 * The top bar's second line: "Daily Issues · 9 words".
 *
 * When [highlight] changes it snaps to the accent colour and fades back over two seconds, so a
 * figure that moves while the user is looking elsewhere on the screen still registers. The fade is
 * driven off the text itself rather than a counter, so any change — up, down, or a locale switch
 * rewriting the words — is announced the same way. The first value shown is not flashed: nothing
 * has changed yet.
 */
@Composable
fun TopBarSubtitle(
    subtitle: String,
    highlight: String?,
    modifier: Modifier = Modifier,
) {
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant

    if (highlight == null) {
        Text(text = subtitle, fontSize = 14.sp, color = baseColor, modifier = modifier)
        return
    }

    // The brand accent, not colorScheme.error: a word count moving is news, not a problem.
    val flashColor = MaterialTheme.colorScheme.primary
    // Animatable rather than animateColorAsState: the flash is snap-then-fade, two target changes
    // that a state-driven animation would collapse into one before a frame ever rendered.
    val highlightColor = remember { Animatable(baseColor) }
    val isFirstValue = remember { mutableStateOf(true) }

    LaunchedEffect(highlight) {
        if (isFirstValue.value) {
            isFirstValue.value = false
        } else {
            highlightColor.snapTo(flashColor)
            highlightColor.animateTo(baseColor, tween(durationMillis = HIGHLIGHT_FADE_MILLIS))
        }
    }

    // Follow a theme switch while sitting at rest, so the fade never lands on a stale colour.
    LaunchedEffect(baseColor) {
        if (!highlightColor.isRunning) highlightColor.snapTo(baseColor)
    }

    Text(
        text = buildAnnotatedString {
            append(subtitle)
            append(SEPARATOR)
            withStyle(SpanStyle(color = highlightColor.value)) { append(highlight) }
        },
        fontSize = 14.sp,
        color = baseColor,
        modifier = modifier,
    )
}
