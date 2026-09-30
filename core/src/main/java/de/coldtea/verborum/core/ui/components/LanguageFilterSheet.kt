package de.coldtea.verborum.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.core.utils.ResStrings

/** Bottom sheet listing "Any language" plus every supported language, filtering by [selected]. */
@Composable
fun LanguageFilterSheet(
    title: String,
    selected: SupportedLanguage?,
    onSelect: (SupportedLanguage?) -> Unit,
    onDismiss: () -> Unit,
) {
    // buildList/forEach are inline, so stringResource is valid inside them.
    val options = buildList {
        add(
            SelectionOption(
                label = stringResource(ResStrings.dictionaryListAnyLanguage),
                isSelected = selected == null,
                onSelect = { onSelect(null) },
            )
        )
        SupportedLanguage.entries.forEach { language ->
            add(
                SelectionOption(
                    label = stringResource(language.displayNameRes),
                    isSelected = selected == language,
                    onSelect = { onSelect(language) },
                )
            )
        }
    }
    SelectionBottomSheet(title = title, options = options, onDismiss = onDismiss)
}
