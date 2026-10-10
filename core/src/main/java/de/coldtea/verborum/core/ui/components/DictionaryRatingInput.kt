package de.coldtea.verborum.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.model.DictionaryRatingUi
import de.coldtea.verborum.core.utils.ResDrawables
import de.coldtea.verborum.core.utils.ResStrings

/**
 * The user's rating of a dictionary they downloaded, as a card that expands. Collapsed it is one
 * line — their stars (small) if they rated it, "Tap to rate" if not — with a down chevron;
 * expanded it holds the five tappable stars with an up chevron in the top corner. Tapping a star
 * rates, and tapping the selected star again removes the rating (guide §6.10) — the caller decides
 * which. Shared by bibliotheca's details screen and the Forum's.
 */
@Composable
fun DictionaryRatingInput(
    rating: DictionaryRatingUi,
    onStarClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) EXPANDED_ROTATION else COLLAPSED_ROTATION,
        label = "ratingChevron",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = stringResource(
                            if (expanded) ResStrings.ratingHide else ResStrings.ratingShow
                        ),
                    ) { expanded = !expanded }
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    when {
                        expanded -> Text(
                            text = stringResource(ResStrings.ratingYourRating),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        rating.stars != null -> SmallStars(stars = rating.stars)
                        else -> Text(
                            text = stringResource(ResStrings.ratingTapToRate),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    painter = painterResource(ResDrawables.ic_chevron_right_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation),
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        (1..STAR_COUNT).forEach { star ->
                            IconButton(
                                onClick = { onStarClick(star) },
                                enabled = !rating.isUpdating,
                                // A star button is at least 48dp wide, so the row stays easy to hit.
                                modifier = Modifier.semantics { selected = star == rating.stars },
                            ) {
                                Icon(
                                    painter = painterResource(starDrawable(filled = star <= (rating.stars ?: 0))),
                                    contentDescription = stringResource(ResStrings.ratingStarDescription, star),
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(ResStrings.ratingHint),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** The collapsed card's summary: the user's stars, read out as one phrase, not five icons. */
@Composable
private fun SmallStars(stars: Int, starSize: Dp = 18.dp) {
    val description = stringResource(ResStrings.ratingCurrent, stars)
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        (1..STAR_COUNT).forEach { star ->
            Icon(
                painter = painterResource(starDrawable(filled = star <= stars)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

private fun starDrawable(filled: Boolean) =
    if (filled) ResDrawables.ic_star_24 else ResDrawables.ic_star_outline_24

private const val STAR_COUNT = 5

// The chevron is drawn pointing right: a quarter turn points it down, three quarters up.
private const val COLLAPSED_ROTATION = 90f
private const val EXPANDED_ROTATION = 270f

@PreviewLightDark
@Composable
private fun DictionaryRatingInputPreview() {
    VerborumTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DictionaryRatingInput(rating = DictionaryRatingUi(stars = 4), onStarClick = {})
            DictionaryRatingInput(rating = DictionaryRatingUi(stars = null), onStarClick = {})
        }
    }
}
