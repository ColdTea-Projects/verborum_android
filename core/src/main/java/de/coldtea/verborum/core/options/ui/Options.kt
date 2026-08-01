package de.coldtea.verborum.core.options.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.utils.ResDrawables
import de.coldtea.verborum.core.utils.ResStrings
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar

/**
 * The Options tab: a vertical list of tappable rows. Today it holds only "Log out", but it is built
 * as a list of [OptionRow]s so future entries (profile, preferences, about…) are a one-line add.
 */
@Composable
fun OptionsScreen(
    onLanguageClick: () -> Unit,
    viewModel: OptionsViewModel = hiltViewModel(),
) {
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    RegisterTopBar(
        title = stringResource(ResStrings.optionsScreenTitle),
        showBackButton = false,
    )

    OptionsContent(
        isLoggingOut = isLoggingOut,
        onLanguageClick = onLanguageClick,
        onLogout = viewModel::logout,
    )
}

@Composable
private fun OptionsContent(
    isLoggingOut: Boolean,
    onLanguageClick: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OptionRow(
            iconRes = ResDrawables.ic_language_24,
            label = stringResource(ResStrings.optionsLanguage),
            onClick = onLanguageClick,
        )

        OptionRow(
            iconRes = ResDrawables.ic_logout_24,
            label = stringResource(ResStrings.optionsLogout),
            onClick = onLogout,
            enabled = !isLoggingOut,
            // Log out is a destructive, session-ending action.
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

/**
 * One tappable options entry. Kept generic (icon + label + click) so the screen can grow without a
 * bespoke composable per item.
 */
@Composable
private fun OptionRow(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = tint,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewOptionsScreen() {
    VerborumTheme {
        OptionsContent(isLoggingOut = false, onLanguageClick = {}, onLogout = {})
    }
}
