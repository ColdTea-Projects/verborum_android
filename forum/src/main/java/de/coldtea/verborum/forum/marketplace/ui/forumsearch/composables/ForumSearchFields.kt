package de.coldtea.verborum.forum.marketplace.ui.forumsearch.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.ui.model.DictionaryTag
import de.coldtea.verborum.core.ui.model.EXAM_TAGS
import de.coldtea.verborum.core.ui.model.LEVEL_TAGS
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.core.ui.model.TOPIC_TAGS
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.languagePairLabel
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import kotlinx.coroutines.launch

/**
 * The chosen language pairs as removable chips, with the add button flowing after the last one.
 * The add button opens [LanguagePairSheet]; tapping a chip removes its pair.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagePairField(
    pairs: List<ForumLanguagePair>,
    onAddPair: (SupportedLanguage, SupportedLanguage) -> Unit,
    onRemovePair: (ForumLanguagePair) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        LanguagePairSheet(
            onPairPicked = onAddPair,
            onDismiss = { showSheet = false },
        )
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        pairs.forEach { pair ->
            val label = languagePairLabel(pair)
            InputChip(
                selected = true,
                onClick = { onRemovePair(pair) },
                label = { Text(text = label) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(CoreResDrawables.ic_close_24),
                        contentDescription = stringResource(ResStrings.forumRemoveLanguagePair, label),
                        modifier = Modifier.size(InputChipDefaults.IconSize),
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
        FilledTonalIconButton(onClick = { showSheet = true }) {
            Icon(
                painter = painterResource(CoreResDrawables.ic_plus_24),
                contentDescription = stringResource(ResStrings.forumAddLanguagePair),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Picks two languages: the first tap marks a language, the second (a different one) completes the
 * pair, hands it to [onPairPicked] and closes the sheet. Tapping the marked language unmarks it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePairSheet(
    onPairPicked: (SupportedLanguage, SupportedLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var firstPick by remember { mutableStateOf<SupportedLanguage?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = stringResource(ResStrings.forumLanguagePairSheetTitle),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = firstPick?.let { stringResource(ResStrings.forumLanguagePairPickSecond, stringResource(it.displayNameRes)) }
                    ?: stringResource(ResStrings.forumLanguagePairPickFirst),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                items(SupportedLanguage.entries) { language ->
                    val isPicked = language == firstPick
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Checkbox) {
                                val first = firstPick
                                when {
                                    first == null -> firstPick = language
                                    first == language -> firstPick = null
                                    else -> {
                                        onPairPicked(first, language)
                                        // Slide the sheet away before removing it.
                                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(language.displayNameRes),
                            fontSize = 16.sp,
                            fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isPicked) {
                            Icon(
                                painter = painterResource(CoreResDrawables.ic_check_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Every tag as a multi-select chip, grouped Level / Topic / Exams like the create form's picker. */
@Composable
fun TagFilterGroups(
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TagGroup(stringResource(CoreResStrings.createDictionaryTagsLevel), LEVEL_TAGS, selectedTags, onToggleTag)
        TagGroup(stringResource(CoreResStrings.createDictionaryTagsTopic), TOPIC_TAGS, selectedTags, onToggleTag)
        TagGroup(stringResource(CoreResStrings.createDictionaryTagsExams), EXAM_TAGS, selectedTags, onToggleTag)
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
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
                // Selected chips match the Search button (primary / onPrimary).
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}
