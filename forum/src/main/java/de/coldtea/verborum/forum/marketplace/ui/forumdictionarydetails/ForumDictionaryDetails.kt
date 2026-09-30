package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.components.DictionaryTagsText
import de.coldtea.verborum.core.ui.components.ScreenError
import de.coldtea.verborum.core.ui.components.languagePairLabel
import de.coldtea.verborum.forum.common.utils.CoreResPlurals
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResDrawables
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.composables.ForumDictionaryInfoPanel
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.composables.ForumWordListItem
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumDictionaryDetailState
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

/**
 * A marketplace dictionary, laid out like bibliotheca's details screen: an info panel instead of
 * the practice buttons, a read-only word list, and a single Download action.
 */
@Composable
fun ForumDictionaryDetailsScreen(
    viewModel: ForumDictionaryDetailsViewModel = hiltViewModel(),
    onDownloadClick: () -> Unit = {},
) {
    val state by viewModel.dictionaryDetailState.collectAsState()

    when (val detailState = state) {
        is ForumDictionaryDetailState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is ForumDictionaryDetailState.Failed -> {
            RegisterTopBar(title = stringResource(CoreResStrings.errorScreenTitle), showBackButton = true)
            ScreenError(onRetry = viewModel::retry)
        }

        is ForumDictionaryDetailState.Success -> {
            val dictionary = detailState.dictionary
            val languagePair = languagePairLabel(dictionary.fromLang, dictionary.toLang)
            val wordCount = pluralStringResource(
                CoreResPlurals.dictionaryListScreenWordCount,
                detailState.words.size,
                detailState.words.size,
            )
            RegisterTopBar(
                title = dictionary.name,
                subtitle = "$languagePair · $wordCount",
                showBackButton = true,
            )

            ForumDictionaryDetailsContent(
                dictionary = dictionary,
                words = detailState.words,
                onDownloadClick = onDownloadClick,
            )
        }
    }
}

@Composable
private fun ForumDictionaryDetailsContent(
    dictionary: ForumDictionaryUi,
    words: List<ForumWordUi>,
    onDownloadClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ForumDictionaryInfoPanel(
                    publisherName = dictionary.publisherName,
                    rating = dictionary.rating,
                    downloadCount = dictionary.downloadCount,
                )

                // The same translated tag line bibliotheca shows under its practice buttons;
                // hidden when there are none.
                if (dictionary.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DictionaryTagsText(tagCodes = dictionary.tags)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Word List Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(ResStrings.forumDetailsWordListHeader),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            words.forEach { word -> ForumWordListItem(word = word) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Sticky bottom action — Download has no behaviour yet.
        Button(
            onClick = onDownloadClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(ResDrawables.ic_download_24),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(ResStrings.forumDownload),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ForumDictionaryDetailsContentPreview() {
    VerborumTheme {
        ForumDictionaryDetailsContent(
            dictionary = ForumDictionaryUi(
                dictionaryId = "1",
                publisherId = "p1",
                publisherName = "Anna Schmidt",
                name = "Everyday German",
                fromLang = "EN",
                toLang = "DE",
                downloadCount = 1284,
                publishedAt = System.currentTimeMillis(),
                rating = 4.7f,
                wordCount = 2,
                tags = listOf("a1", "daily_routine", "shopping"),
            ),
            words = listOf(
                ForumWordUi(wordId = "w1", word = "house", translation = "das Haus"),
                ForumWordUi(wordId = "w2", word = "to buy", translation = "kaufen/erwerben"),
            ),
            onDownloadClick = {},
        )
    }
}
