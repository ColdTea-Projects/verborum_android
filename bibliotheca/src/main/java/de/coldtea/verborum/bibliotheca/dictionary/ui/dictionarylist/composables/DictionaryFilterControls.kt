package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.ui.components.FilterBarChip
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionarySort
import de.coldtea.verborum.bibliotheca.common.utils.CoreResStrings
import de.coldtea.verborum.bibliotheca.common.utils.CoreResDrawables

/** The expandable "Search dictionaries" field shown above the filter chips. */
@Composable
fun DictionarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(CoreResDrawables.ic_search_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(ResStrings.dictionaryListSearchHint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                },
            )
            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(CoreResDrawables.ic_close_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onQueryChange("") },
                )
            }
        }
    }
}

/**
 * The horizontally scrollable row of chips: From / To language filters, the sort chip, and a Clear
 * chip. Each opens its own bottom sheet except Clear, which resets everything.
 */
@Composable
fun DictionaryFilterBar(
    fromFilter: SupportedLanguage?,
    toFilter: SupportedLanguage?,
    sortOrder: DictionarySort,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSortClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val anyLabel = stringResource(CoreResStrings.dictionaryListFilterAny)
    val fromValue = fromFilter?.let { stringResource(it.displayNameRes) } ?: anyLabel
    val toValue = toFilter?.let { stringResource(it.displayNameRes) } ?: anyLabel

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListFromChip, fromValue),
            onClick = onFromClick,
            trailingCaret = true,
            highlighted = fromFilter != null,
        )
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListToChip, toValue),
            onClick = onToClick,
            trailingCaret = true,
            highlighted = toFilter != null,
        )
        FilterBarChip(
            text = stringResource(sortOrder.labelRes),
            onClick = onSortClick,
            leadingIconRes = ResDrawables.ic_sort_24,
            trailingCaret = true,
        )
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListClear),
            onClick = onClearClick,
            leadingIconRes = CoreResDrawables.ic_close_24,
        )
    }
}
