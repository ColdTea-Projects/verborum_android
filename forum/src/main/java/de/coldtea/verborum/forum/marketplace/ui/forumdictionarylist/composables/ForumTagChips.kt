package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.components.dictionaryTagLabels

/** A card shows at most this many tags; the rest stay hidden to keep the card compact. */
const val MAX_VISIBLE_TAG_CHIPS = 4

/**
 * A dictionary's tags as small red chips with white text — the colours of the selected tag chips on
 * the create-dictionary screen (primary / onPrimary). Only the first [MAX_VISIBLE_TAG_CHIPS]
 * resolvable tags are shown; unknown codes are skipped before counting. Renders nothing when there
 * are no resolvable tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForumTagChips(
    tagCodes: List<String>,
    modifier: Modifier = Modifier,
) {
    val labels = dictionaryTagLabels(tagCodes).take(MAX_VISIBLE_TAG_CHIPS)
    if (labels.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        labels.forEach { label -> TagChip(label = label) }
    }
}

@Composable
private fun TagChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ForumTagChipsPreview() {
    VerborumTheme {
        ForumTagChips(tagCodes = listOf("basic", "a1", "food_drink", "travel", "cils"))
    }
}
