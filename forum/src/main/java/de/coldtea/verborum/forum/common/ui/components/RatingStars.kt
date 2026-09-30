package de.coldtea.verborum.forum.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.forum.common.utils.ResDrawables
import de.coldtea.verborum.forum.common.utils.ResStrings
import kotlin.math.roundToInt

/**
 * [rating] (0–5) as five stars in the gold accent, rounded to the nearest half. Read out as one
 * phrase ("Rated 4.5 out of 5") instead of five separate icons. Shared by the Forum list cards and
 * the details screen's info panel.
 */
@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    val halves = (rating.coerceIn(0f, STAR_COUNT.toFloat()) * 2).roundToInt()
    val description = stringResource(ResStrings.forumRatingContentDescription, formatRating(rating))

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(STAR_COUNT) { index ->
            val iconRes = when {
                halves >= (index + 1) * 2 -> ResDrawables.ic_star_24
                halves == index * 2 + 1 -> ResDrawables.ic_star_half_24
                else -> ResDrawables.ic_star_outline_24
            }
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

/** One decimal, localized ("4.5" / "4,5"). */
internal fun formatRating(rating: Float): String = "%.1f".format(rating)

private const val STAR_COUNT = 5

@PreviewLightDark
@Composable
private fun RatingStarsPreview() {
    VerborumTheme {
        RatingStars(rating = 3.6f)
    }
}
