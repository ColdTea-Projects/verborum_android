package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.model.SupportedLanguage

/**
 * Asks which way the test runs before it starts: the first language is the question, the second
 * the choices. [onDirectionSelected] receives `reversed = true` for the second option.
 */
@Composable
fun TestDirectionDialog(
    fromLang: String,
    toLang: String,
    onDirectionSelected: (reversed: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val from = languageName(fromLang)
    val to = languageName(toLang)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.testDirectionDialogTitle)) },
        text = {
            Column {
                DirectionOption("$from → $to") { onDirectionSelected(false) }
                DirectionOption("$to → $from") { onDirectionSelected(true) }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDialogCancel))
            }
        },
    )
}

@Composable
private fun DirectionOption(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun languageName(code: String): String =
    SupportedLanguage.fromCode(code)?.let { stringResource(it.displayNameRes) } ?: code

@PreviewLightDark
@Composable
private fun TestDirectionDialogPreview() {
    VerborumTheme {
        TestDirectionDialog(
            fromLang = "en",
            toLang = "de",
            onDirectionSelected = {},
            onDismiss = {},
        )
    }
}
