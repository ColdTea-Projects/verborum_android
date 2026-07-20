package de.coldtea.verborum.bibliotheca.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.core.theme.VerborumTheme

/**
 * The shared full-screen error shown when a screen's data could not be loaded (its state is
 * `Failed`). One implementation so every screen's load-failure looks and behaves the same:
 * a message and a Retry action. Mutation failures use a snackbar instead and never reach here.
 */
@Composable
fun ScreenError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(ResStrings.errorScreenMessage),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(text = stringResource(ResStrings.errorRetry))
        }
    }
}

@PreviewLightDark
@Composable
private fun ScreenErrorPreview() {
    VerborumTheme {
        ScreenError(onRetry = {})
    }
}
