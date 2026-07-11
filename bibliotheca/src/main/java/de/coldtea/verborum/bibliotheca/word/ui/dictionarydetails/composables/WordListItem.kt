package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun WordListItem(
    modifier: Modifier = Modifier,
    word: WordUi,
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            // Progress bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(word.level / 7f)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.word,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    // weight keeps the action icons anchored; ellipsis clips overlong words.
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEditClick(word.wordId) }) {
                        Icon(
                            painter = painterResource(ResDrawables.ic_edit_24),
                            contentDescription = stringResource(ResStrings.dictionaryDetailsScreenEditWord),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { onDeleteClick(word.wordId) }) {
                        Icon(
                            painter = painterResource(ResDrawables.ic_delete_24),
                            contentDescription = stringResource(ResStrings.dictionaryDetailsScreenDeleteWord),
                            // The drawable carries its own colors — don't tint over them.
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun PreviewWordListItem() {
    VerborumTheme {
        WordListItem(
            word = WordUi(
                wordId = "wordId",
                dictionaryId = "dictionaryId",
                word = "word",
                wordMeta = "wordMeta",
                translation = "translation",
                translationMeta = "translationMeta",
                level = 2,
                createdAt = 0L,
                updatedAt = 0L,
            )
        )
    }
}