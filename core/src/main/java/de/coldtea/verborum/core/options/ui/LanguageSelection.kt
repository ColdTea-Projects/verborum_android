package de.coldtea.verborum.core.options.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.options.ui.model.AppLanguage
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.utils.ResDrawables
import de.coldtea.verborum.core.utils.ResStrings

/**
 * Picks the language the app's own UI is shown in. "System default" leads and is the state a fresh
 * install is in; picking anything else overrides the device language for this app alone.
 *
 * Choosing a language recreates the activity (AppCompat applies per-app locales that way), so this
 * screen redraws in the newly picked language rather than needing to navigate back.
 */
@Composable
fun LanguageSelectionScreen(viewModel: LanguageSelectionViewModel = hiltViewModel()) {
    val selected by viewModel.selected.collectAsState()

    RegisterTopBar(title = stringResource(ResStrings.optionsLanguage))

    LanguageSelectionContent(
        selected = selected,
        onSelect = viewModel::select,
    )
}

@Composable
private fun LanguageSelectionContent(
    selected: AppLanguage?,
    onSelect: (AppLanguage?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            LanguageRow(
                label = stringResource(ResStrings.optionsLanguageSystemDefault),
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(AppLanguage.entries) { language ->
            LanguageRow(
                // The endonym is the language's own name, so it is never translated.
                label = language.endonym,
                selected = selected == language,
                onClick = { onSelect(language) },
            )
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // selectable (not clickable) so TalkBack announces the row's state, not just its text.
            .selectable(selected = selected, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (selected) {
                Icon(
                    painter = painterResource(ResDrawables.ic_check_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLanguageSelection() {
    VerborumTheme {
        LanguageSelectionContent(selected = AppLanguage.GERMAN, onSelect = {})
    }
}
