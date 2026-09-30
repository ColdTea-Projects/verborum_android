package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi

/**
 * A read-only entry of a marketplace dictionary: the word with its translation beneath it.
 * Stacked and never truncated — both wrap to as many lines as they need, so the user sees every
 * entry in full before downloading it.
 */
@Composable
fun ForumWordListItem(
    word: ForumWordUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = word.word,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = word.translation,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ForumWordListItemPreview() {
    VerborumTheme {
        ForumWordListItem(word = ForumWordUi(wordId = "w1", word = "to buy", translation = "kaufen/erwerben"))
    }
}

@PreviewLightDark
@Composable
private fun ForumWordListItemLongEntryPreview() {
    VerborumTheme {
        ForumWordListItem(
            word = ForumWordUi(
                wordId = "w2",
                word = "to look forward to something you have been waiting for a long time",
                translation = "sich auf etwas freuen, auf das man schon sehr lange gewartet hat",
            )
        )
    }
}
