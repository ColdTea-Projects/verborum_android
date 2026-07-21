package de.coldtea.verborum.bibliotheca.dictionary.ui.composables

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionarySort

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
                painter = painterResource(ResDrawables.ic_search_24),
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
                    painter = painterResource(ResDrawables.ic_close_24),
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
    val anyLabel = stringResource(ResStrings.dictionaryListFilterAny)
    val fromValue = fromFilter?.let { stringResource(it.displayNameRes) } ?: anyLabel
    val toValue = toFilter?.let { stringResource(it.displayNameRes) } ?: anyLabel

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            text = stringResource(ResStrings.dictionaryListFromChip, fromValue),
            onClick = onFromClick,
            trailingCaret = true,
            highlighted = fromFilter != null,
        )
        FilterChip(
            text = stringResource(ResStrings.dictionaryListToChip, toValue),
            onClick = onToClick,
            trailingCaret = true,
            highlighted = toFilter != null,
        )
        FilterChip(
            text = stringResource(sortOrder.labelRes),
            onClick = onSortClick,
            leadingIconRes = ResDrawables.ic_sort_24,
            trailingCaret = true,
        )
        FilterChip(
            text = stringResource(ResStrings.dictionaryListClear),
            onClick = onClearClick,
            leadingIconRes = ResDrawables.ic_close_24,
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    onClick: () -> Unit,
    leadingIconRes: Int? = null,
    trailingCaret: Boolean = false,
    highlighted: Boolean = false,
) {
    val accent = MaterialTheme.colorScheme.primary
    val contentColor = if (highlighted) accent else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = if (highlighted) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (highlighted) accent else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leadingIconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
            if (trailingCaret) {
                // chevron-right rotated a quarter-turn = a downward caret.
                Icon(
                    painter = painterResource(ResDrawables.ic_chevron_right_24),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(90f),
                )
            }
        }
    }
}
