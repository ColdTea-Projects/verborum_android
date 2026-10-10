package de.coldtea.verborum.core.profile.ui.profile.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.utils.ResStrings

/**
 * Confirmation for leaving the Forum (guide §6.2): the server makes every dictionary of the user
 * private, and people who downloaded them lose access.
 */
@Composable
fun LeaveForumDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.leaveForumDialogTitle)) },
        text = { Text(text = stringResource(ResStrings.leaveForumDialogMessage)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(ResStrings.leaveForumDialogConfirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ResStrings.leaveForumDialogCancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun LeaveForumDialogPreview() {
    VerborumTheme { LeaveForumDialog(onConfirm = {}, onDismiss = {}) }
}
