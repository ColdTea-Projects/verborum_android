package de.coldtea.verborum.core.profile.ui.deleteaccount.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.utils.ResStrings

private val DELETED_ITEMS = listOf(
    ResStrings.deleteAccountItemProfile,
    ResStrings.deleteAccountItemDictionaries,
    ResStrings.deleteAccountItemVault,
    ResStrings.deleteAccountItemRatings,
    ResStrings.deleteAccountItemDevices,
    ResStrings.deleteAccountItemSignIn,
)

/**
 * Step A (`docs/account-deletion-guide.md` §6): what is deleted, that it cannot be undone, and the
 * "I understand" checkbox that unlocks Continue. The body scrolls, so a small screen or a large font
 * never hides the checkbox or the buttons.
 */
@Composable
fun DeleteAccountWarningDialog(
    acknowledged: Boolean,
    onAcknowledgedChange: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.deleteAccountTitle)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = stringResource(ResStrings.deleteAccountIntro))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(ResStrings.deleteAccountDeletesHeader),
                    fontWeight = FontWeight.SemiBold,
                )
                DELETED_ITEMS.forEach { item ->
                    Text(text = "• " + stringResource(item), modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(ResStrings.deleteAccountIrreversible),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(ResStrings.deleteAccountOthers))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(ResStrings.deleteAccountSignInNotice))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(ResStrings.deleteAccountNewAccount))
                Spacer(modifier = Modifier.height(16.dp))
                // The whole row is the touch target, as in the Forum terms.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = acknowledged,
                            role = Role.Checkbox,
                            onValueChange = onAcknowledgedChange,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = acknowledged, onCheckedChange = null)
                    Text(
                        text = stringResource(ResStrings.deleteAccountAcknowledge),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue, enabled = acknowledged) {
                Text(
                    text = stringResource(ResStrings.deleteAccountContinue),
                    color = if (acknowledged) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(ResStrings.deleteAccountCancel)) }
        },
    )
}

/** Step D: the last question, shown only after a fresh sign-in as the same account. */
@Composable
fun DeleteAccountConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.deleteAccountConfirmTitle)) },
        text = { Text(text = stringResource(ResStrings.deleteAccountConfirmMessage)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(ResStrings.deleteAccountConfirmDelete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(ResStrings.deleteAccountCancel)) }
        },
    )
}

/**
 * Blocks the screen while the sign-in result is checked or the server call runs. It cannot be
 * dismissed: leaving a delete half-way gives no benefit and hides the result. [message] is null while
 * only the sign-in is checked — "Deleting…" would be untrue then.
 */
@Composable
fun DeleteAccountProgressDialog(message: String?) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                if (message != null) {
                    Text(text = message, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DeleteAccountWarningDialogPreview() {
    VerborumTheme {
        DeleteAccountWarningDialog(acknowledged = true, onAcknowledgedChange = {}, onContinue = {}, onDismiss = {})
    }
}

@PreviewLightDark
@Composable
private fun DeleteAccountConfirmDialogPreview() {
    VerborumTheme { DeleteAccountConfirmDialog(onConfirm = {}, onDismiss = {}) }
}
