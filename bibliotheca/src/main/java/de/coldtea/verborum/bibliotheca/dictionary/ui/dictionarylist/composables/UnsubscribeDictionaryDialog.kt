package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * Confirmation for unsubscribing from a downloaded dictionary — the counterpart of
 * [DeleteDictionaryDialog], shared by the dictionary list and the details screen.
 */
@Composable
fun UnsubscribeDictionaryDialog(
    dictionaryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.unsubscribeDialogTitle, dictionaryName)) },
        text = { Text(text = stringResource(ResStrings.unsubscribeDialogMessage)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(ResStrings.dictionaryOptionsUnsubscribe),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDialogCancel))
            }
        },
    )
}
