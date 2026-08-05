package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * Confirmation for the destructive dictionary delete, shared by the dictionary list (long-press
 * options) and the details screen. The confirm action is tinted with the error color.
 */
@Composable
fun DeleteDictionaryDialog(
    dictionaryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDialogTitle)) },
        text = {
            Text(
                text = stringResource(
                    ResStrings.dictionaryDetailsScreenDeleteDialogMessage,
                    dictionaryName,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDialogConfirm),
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
