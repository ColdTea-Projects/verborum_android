package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables

import android.text.format.DateUtils
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.components.languagePairLabel
import de.coldtea.verborum.core.ui.components.relativeTimeAgo
import de.coldtea.verborum.forum.common.ui.components.RatingStars
import de.coldtea.verborum.forum.common.ui.components.formatRating
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResPlurals
import de.coldtea.verborum.forum.common.utils.ResDrawables
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

/**
 * The marketplace counterpart of bibliotheca's `DictionaryCard` — same look, plus the publisher
 * next to the publish date and the dictionary's tags as chips along the bottom.
 */
@Composable
fun ForumDictionaryCard(
    modifier: Modifier = Modifier,
    dictionary: ForumDictionaryUi,
    onClick: (String) -> Unit,
    onMenuClick: (ForumDictionaryUi) -> Unit = {},
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = {
                    isPressed = !isPressed
                    onClick(dictionary.dictionaryId)
                },
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
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            )

            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book Icon — pinned to the top, level with the name, however tall the card grows.
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Top)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(CoreResDrawables.ic_book_24_black),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dictionary.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = languagePairLabel(dictionary.fromLang, dictionary.toLang),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Left out when the backend has no rating (not in the contract yet, guide §9).
                    dictionary.rating?.let { rating ->
                        CardRating(rating = rating, modifier = Modifier.padding(top = 6.dp))
                    }

                    ForumDictionaryMetaLine(
                        dictionary = dictionary,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    ForumTagChips(
                        tagCodes = dictionary.tags,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                // Overflow menu — opens the Download options sheet.
                IconButton(onClick = { onMenuClick(dictionary) }) {
                    Icon(
                        painter = painterResource(ResDrawables.ic_more_vert_24),
                        contentDescription = stringResource(ResStrings.forumMoreOptions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/** The details panel's rating, inline: five stars and the value ("★★★★½ 4.7"). */
@Composable
private fun CardRating(
    rating: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatingStars(rating = rating, starSize = 14.dp)
        Text(
            text = formatRating(rating),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // The stars already announce "Rated 4.7 out of 5"; don't read the number twice.
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

/**
 * "12 words • 3 days ago • by Anna" — same spacing as bibliotheca's card, but flowing onto a second
 * line when a long publisher name would not fit. A missing word count is simply left out.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ForumDictionaryMetaLine(
    dictionary: ForumDictionaryUi,
    modifier: Modifier = Modifier,
) {
    // buildList/let are inline, so the resource lookups inside them are valid composable calls.
    val parts = buildList {
        dictionary.wordCount?.let { count ->
            add(pluralStringResource(CoreResPlurals.dictionaryListScreenWordCount, count, count))
        }
        add(relativeTimeAgo(dictionary.publishedAt))
        add(
            stringResource(
                ResStrings.forumPublishedBy,
                dictionary.publisherName ?: stringResource(ResStrings.forumUnknownPublisher),
            )
        )
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        parts.forEachIndexed { index, part ->
            if (index > 0) MetaText(text = "•")
            MetaText(text = part)
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@PreviewLightDark
@Composable
private fun ForumDictionaryCardPreview() {
    VerborumTheme {
        ForumDictionaryCard(
            dictionary = ForumDictionaryUi(
                dictionaryId = "dictionaryId",
                publisherId = "publisherId",
                publisherName = "Anna Schmidt",
                name = "Everyday German",
                fromLang = "EN",
                toLang = "DE",
                downloadCount = 1284,
                publishedAt = System.currentTimeMillis() - 3 * DateUtils.DAY_IN_MILLIS,
                rating = 4.7f,
                wordCount = 12,
                tags = listOf("a1", "daily_routine", "shopping", "family", "travel"),
            ),
            onClick = {},
        )
    }
}
