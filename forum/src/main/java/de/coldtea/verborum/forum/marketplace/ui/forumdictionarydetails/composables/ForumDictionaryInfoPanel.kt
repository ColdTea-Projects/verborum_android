package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.forum.common.ui.components.RatingStars
import de.coldtea.verborum.forum.common.ui.components.formatRating
import de.coldtea.verborum.forum.common.utils.ResStrings
import java.text.NumberFormat

/**
 * The small panel at the top of a marketplace dictionary — who made it, how it is rated, and how
 * many people downloaded it. Takes the place of bibliotheca's Test/Self Practice buttons.
 */
@Composable
fun ForumDictionaryInfoPanel(
    publisherName: String?,
    rating: Float?,
    downloadCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoColumn(label = stringResource(ResStrings.forumDetailsCreator)) {
                InfoValue(publisherName ?: stringResource(ResStrings.forumUnknownPublisher))
            }

            InfoColumn(label = stringResource(ResStrings.forumDetailsRating)) {
                if (rating == null) {
                    InfoValue(stringResource(ResStrings.forumDetailsNoRating), emphasized = false)
                } else {
                    RatingStars(rating = rating, modifier = Modifier.padding(top = 2.dp))
                    Text(
                        text = formatRating(rating),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            InfoColumn(label = stringResource(ResStrings.forumDetailsDownloads)) {
                InfoValue(NumberFormat.getIntegerInstance().format(downloadCount))
            }
        }
    }
}

@Composable
private fun RowScope.InfoColumn(
    label: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        // Label and value are announced together as one stat.
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Column(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content,
        )
    }
}

@Composable
private fun InfoValue(text: String, emphasized: Boolean = true) {
    Text(
        text = text,
        fontSize = if (emphasized) 15.sp else 13.sp,
        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@PreviewLightDark
@Composable
private fun ForumDictionaryInfoPanelPreview() {
    VerborumTheme {
        ForumDictionaryInfoPanel(publisherName = "Anna Schmidt", rating = 4.7f, downloadCount = 1284)
    }
}
