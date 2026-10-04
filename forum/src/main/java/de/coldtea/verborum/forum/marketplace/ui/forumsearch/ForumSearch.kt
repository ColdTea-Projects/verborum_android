package de.coldtea.verborum.forum.marketplace.ui.forumsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.common.utils.CoreResStrings
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import de.coldtea.verborum.forum.marketplace.ui.forumsearch.composables.LanguagePairField
import de.coldtea.verborum.forum.marketplace.ui.forumsearch.composables.TagFilterGroups

/**
 * The Forum's search criteria on a screen of their own, laid out like bibliotheca's create form:
 * the fields scroll, and the sticky Search button hands the draft to [onSearch] (which applies it
 * to the list and pops back). Back leaves the list's filter untouched.
 */
@Composable
fun ForumSearchScreen(
    viewModel: ForumSearchViewModel = hiltViewModel(),
    onSearch: (ForumDictionaryFilter) -> Unit,
) {
    val draft by viewModel.draft.collectAsState()

    RegisterTopBar(
        title = stringResource(ResStrings.forumSearchScreenTitle),
        subtitle = stringResource(ResStrings.forumSearchScreenSubtitle),
        showBackButton = true,
    )

    ForumSearchContent(
        draft = draft,
        onAddLanguagePair = viewModel::onAddLanguagePair,
        onRemoveLanguagePair = viewModel::onRemoveLanguagePair,
        onToggleTag = viewModel::onToggleTag,
        onUserNameChange = viewModel::onUserNameChange,
        onClear = viewModel::clear,
        onSearch = { onSearch(viewModel.searchFilter()) },
    )
}

@Composable
private fun ForumSearchContent(
    draft: ForumDictionaryFilter,
    onAddLanguagePair: (SupportedLanguage, SupportedLanguage) -> Unit,
    onRemoveLanguagePair: (ForumLanguagePair) -> Unit,
    onToggleTag: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        // Everything above the sticky buttons scrolls: the tag chips overflow the screen.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle(stringResource(ResStrings.forumSearchLanguagePairs))
            Text(
                text = stringResource(ResStrings.forumSearchLanguagePairsHint),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LanguagePairField(
                pairs = draft.languagePairs,
                onAddPair = onAddLanguagePair,
                onRemovePair = onRemoveLanguagePair,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(ResStrings.forumSearchUser))
            OutlinedTextField(
                value = draft.userName.orEmpty(),
                onValueChange = onUserNameChange,
                label = { Text(text = stringResource(ResStrings.forumFilterUserHint)) },
                singleLine = true,
                // The keyboard's search key runs the search, like the sticky button.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(ResStrings.forumFilterTagsTitle))
            TagFilterGroups(selectedTags = draft.tags, onToggleTag = onToggleTag)

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky bottom actions: reset the draft, or apply it and return to the list.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(CoreResStrings.dictionaryListClear),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Button(
                onClick = onSearch,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(CoreResStrings.dictionaryListSearch),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@PreviewLightDark
@Composable
private fun ForumSearchContentPreview() {
    VerborumTheme {
        ForumSearchContent(
            draft = ForumDictionaryFilter(
                languagePairs = listOf(
                    ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH),
                    ForumLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH),
                ),
                tags = setOf("a1", "travel"),
                userName = "anna",
            ),
            onAddLanguagePair = { _, _ -> },
            onRemoveLanguagePair = {},
            onToggleTag = {},
            onUserNameChange = {},
            onClear = {},
            onSearch = {},
        )
    }
}
