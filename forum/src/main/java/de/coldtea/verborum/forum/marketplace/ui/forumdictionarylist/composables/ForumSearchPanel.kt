package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.components.FilterBarChip
import de.coldtea.verborum.core.ui.components.LanguageFilterSheet
import de.coldtea.verborum.core.ui.components.MultiSelectionBottomSheet
import de.coldtea.verborum.core.ui.components.SelectionOption
import de.coldtea.verborum.core.ui.components.SelectionSection
import de.coldtea.verborum.core.ui.model.DictionaryTag
import de.coldtea.verborum.core.ui.model.EXAM_TAGS
import de.coldtea.verborum.core.ui.model.LEVEL_TAGS
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.core.ui.model.TOPIC_TAGS
import de.coldtea.verborum.core.ui.model.dictionaryTagByCode
import de.coldtea.verborum.forum.common.utils.CoreResDrawables
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter

/**
 * The Forum list's search panel, shaped like bibliotheca's: a scrollable row of filter chips —
 * From / To language, Tags, User — plus Clear, and the user-name field that the User chip reveals
 * beneath them. Owns only which picker sheet is open; every criterion lives in [filter].
 */
@Composable
fun ForumSearchPanel(
    filter: ForumDictionaryFilter,
    userNameInputVisible: Boolean,
    onFromFilterChange: (SupportedLanguage?) -> Unit,
    onToFilterChange: (SupportedLanguage?) -> Unit,
    onToggleTag: (String) -> Unit,
    onUserClick: () -> Unit,
    onUserNameSearch: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFromSheet by remember { mutableStateOf(false) }
    var showToSheet by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }

    if (showFromSheet) {
        LanguageFilterSheet(
            title = stringResource(CoreResStrings.createDictionaryScreenFromLanguage),
            selected = filter.fromLanguage,
            onSelect = onFromFilterChange,
            onDismiss = { showFromSheet = false },
        )
    }
    if (showToSheet) {
        LanguageFilterSheet(
            title = stringResource(CoreResStrings.createDictionaryScreenToLanguage),
            selected = filter.toLanguage,
            onSelect = onToFilterChange,
            onDismiss = { showToSheet = false },
        )
    }
    if (showTagSheet) {
        TagFilterSheet(
            selected = filter.tags,
            onToggleTag = onToggleTag,
            onDismiss = { showTagSheet = false },
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ForumFilterBar(
            filter = filter,
            onFromClick = { showFromSheet = true },
            onToClick = { showToSheet = true },
            onTagsClick = { showTagSheet = true },
            onUserClick = onUserClick,
            onClearClick = onClearClick,
        )
        AnimatedVisibility(visible = userNameInputVisible) {
            UserNameSearchField(
                appliedUserName = filter.userName,
                onSearch = onUserNameSearch,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun ForumFilterBar(
    filter: ForumDictionaryFilter,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onTagsClick: () -> Unit,
    onUserClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    val anyLabel = stringResource(CoreResStrings.dictionaryListFilterAny)
    val fromValue = filter.fromLanguage?.let { stringResource(it.displayNameRes) } ?: anyLabel
    val toValue = filter.toLanguage?.let { stringResource(it.displayNameRes) } ?: anyLabel
    // One tag reads as its label, several as their count.
    val tagsValue = when (filter.tags.size) {
        0 -> anyLabel
        1 -> filter.tags.single().let { code -> dictionaryTagByCode(code)?.label() ?: code }
        else -> filter.tags.size.toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListFromChip, fromValue),
            onClick = onFromClick,
            trailingCaret = true,
            highlighted = filter.fromLanguage != null,
        )
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListToChip, toValue),
            onClick = onToClick,
            trailingCaret = true,
            highlighted = filter.toLanguage != null,
        )
        FilterBarChip(
            text = stringResource(ResStrings.forumFilterTagsChip, tagsValue),
            onClick = onTagsClick,
            trailingCaret = true,
            highlighted = filter.tags.isNotEmpty(),
        )
        FilterBarChip(
            text = stringResource(ResStrings.forumFilterUserChip, filter.userName ?: anyLabel),
            onClick = onUserClick,
            highlighted = filter.userName != null,
        )
        FilterBarChip(
            text = stringResource(CoreResStrings.dictionaryListClear),
            onClick = onClearClick,
            leadingIconRes = CoreResDrawables.ic_close_24,
        )
    }
}

/** The user-name input and its Search button; the name is applied only on Search. */
@Composable
private fun UserNameSearchField(
    appliedUserName: String?,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The draft restarts from the applied name whenever that changes (e.g. after Clear).
    var draft by rememberSaveable(appliedUserName) { mutableStateOf(appliedUserName.orEmpty()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val search = {
        onSearch(draft)
        focusManager.clearFocus()
    }

    // Revealed to be typed into: focus it (and raise the keyboard) right away.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search() }),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (draft.isEmpty()) {
                            Text(
                                text = stringResource(ResStrings.forumFilterUserHint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                            )
                        }
                        innerTextField()
                    },
                )
                if (draft.isNotEmpty()) {
                    Icon(
                        painter = painterResource(CoreResDrawables.ic_close_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { draft = "" },
                    )
                }
            }
        }
        Button(
            onClick = search,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            // Matches the field's height.
            modifier = Modifier.height(52.dp),
        ) {
            Text(
                text = stringResource(CoreResStrings.dictionaryListSearch),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Multi-select tag picker, grouped like the create-dictionary tag selector. */
@Composable
private fun TagFilterSheet(
    selected: Set<String>,
    onToggleTag: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sections = listOf(
        CoreResStrings.createDictionaryTagsLevel to LEVEL_TAGS,
        CoreResStrings.createDictionaryTagsTopic to TOPIC_TAGS,
        CoreResStrings.createDictionaryTagsExams to EXAM_TAGS,
    ).map { (titleRes, tags) ->
        SelectionSection(
            title = stringResource(titleRes),
            options = tags.map { tag ->
                SelectionOption(
                    label = tag.label(),
                    isSelected = tag.code in selected,
                    onSelect = { onToggleTag(tag.code) },
                )
            },
        )
    }
    MultiSelectionBottomSheet(
        title = stringResource(ResStrings.forumFilterTagsTitle),
        sections = sections,
        onDismiss = onDismiss,
    )
}

/** Translatable tags resolve their label from resources; fixed names show as-is. */
@Composable
private fun DictionaryTag.label(): String = labelRes?.let { stringResource(it) } ?: name.orEmpty()

@PreviewLightDark
@Composable
private fun ForumSearchPanelPreview() {
    VerborumTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ForumSearchPanel(
                filter = ForumDictionaryFilter(
                    fromLanguage = SupportedLanguage.ENGLISH,
                    tags = setOf("a1", "travel"),
                    userName = "anna",
                ),
                userNameInputVisible = true,
                onFromFilterChange = {},
                onToFilterChange = {},
                onToggleTag = {},
                onUserClick = {},
                onUserNameSearch = {},
                onClearClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
