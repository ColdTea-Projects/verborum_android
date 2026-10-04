package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.components.FilterBarChip
import de.coldtea.verborum.core.ui.components.dictionaryTagLabels
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair

/**
 * The criteria the list is currently narrowed by, as highlighted chips — only the ones in use —
 * plus Clear. Tapping a chip reopens the search screen; renders nothing for an inactive filter.
 */
@Composable
fun ForumActiveFilters(
    filter: ForumDictionaryFilter,
    onFilterClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!filter.isActive) return

    // One tag reads as its label, several as their count.
    val tagsValue = when (filter.tags.size) {
        0 -> null
        1 -> dictionaryTagLabels(filter.tags.toList()).firstOrNull() ?: filter.tags.single()
        else -> filter.tags.size.toString()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        filter.languagePairs.forEach { pair ->
            ActiveChip(languagePairLabel(pair), onFilterClick)
        }
        tagsValue?.let { value ->
            ActiveChip(stringResource(ResStrings.forumFilterTagsChip, value), onFilterClick)
        }
        filter.userName?.let { userName ->
            ActiveChip(stringResource(ResStrings.forumFilterUserChip, userName), onFilterClick)
        }
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListClear),
            onClick = onClearClick,
            leadingIconRes = CoreResDrawables.ic_close_24,
        )
    }
}

@Composable
private fun ActiveChip(text: String, onClick: () -> Unit) {
    FilterBarChip(text = text, onClick = onClick, highlighted = true)
}

@PreviewLightDark
@Composable
private fun ForumActiveFiltersPreview() {
    VerborumTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ForumActiveFilters(
                filter = ForumDictionaryFilter(
                    languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH)),
                    tags = setOf("a1", "travel"),
                    userName = "anna",
                ),
                onFilterClick = {},
                onClearClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
