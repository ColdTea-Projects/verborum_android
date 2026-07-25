package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.DictionaryTag
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.EXAM_TAGS
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.LEVEL_TAGS
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.TOPIC_TAGS

/**
 * Collapsible tag picker for the create/edit dictionary form: a header the user taps to reveal
 * multi-select chip groups (Level / Topic / Exams).
 *
 * UI-only for now — selection lives in local state and is not yet persisted or sent anywhere. The
 * chip taxonomy below is provisional: level codes (A1, N5, HSK 3…) and exam names are language-
 * neutral, but the Topic labels are English placeholders that will need localizing once the tag
 * data model is finalized.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "tagSelectorChevron",
    )
    // Announced by TalkBack so the expand/collapse state of the header is perceivable.
    val stateLabel = stringResource(
        if (expanded) ResStrings.tagSelectorExpanded else ResStrings.tagSelectorCollapsed
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .semantics { stateDescription = stateLabel }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(ResStrings.createDictionaryTagsTitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(ResStrings.createDictionaryTagsHint),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
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

        AnimatedVisibility(visible = expanded) {
            Column {
                TagGroup(
                    title = stringResource(ResStrings.createDictionaryTagsLevel),
                    tags = LEVEL_TAGS,
                    selectedTags = selectedTags,
                    onToggleTag = onToggleTag,
                )
                TagGroup(
                    title = stringResource(ResStrings.createDictionaryTagsTopic),
                    tags = TOPIC_TAGS,
                    selectedTags = selectedTags,
                    onToggleTag = onToggleTag,
                )
                TagGroup(
                    title = stringResource(ResStrings.createDictionaryTagsExams),
                    tags = EXAM_TAGS,
                    selectedTags = selectedTags,
                    onToggleTag = onToggleTag,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagGroup(
    title: String,
    tags: List<DictionaryTag>,
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            // Translatable tags resolve their label from resources; fixed names show as-is.
            val label = tag.labelRes?.let { stringResource(it) } ?: tag.name.orEmpty()
            FilterChip(
                selected = tag.code in selectedTags,
                onClick = { onToggleTag(tag.code) },
                label = { Text(text = label) },
                // Selected chips match the Create button (primary / onPrimary).
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}
