package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionarySharingUi
import de.coldtea.verborum.core.theme.VerborumTheme

/**
 * "Shared in the Forum" for one of a member's own dictionaries. Unsharing is confirmed first —
 * people who downloaded it lose access — and the member's last shared dictionary is locked on.
 */
@Composable
fun DictionarySharingSwitch(
    sharing: DictionarySharingUi,
    dictionaryName: String,
    onSharedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmUnshare by rememberSaveable { mutableStateOf(false) }

    if (confirmUnshare) {
        AlertDialog(
            onDismissRequest = { confirmUnshare = false },
            title = { Text(text = stringResource(ResStrings.unshareDialogTitle, dictionaryName)) },
            text = { Text(text = stringResource(ResStrings.unshareDialogMessage)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmUnshare = false
                    onSharedChange(false)
                }) {
                    Text(text = stringResource(ResStrings.unshareDialogConfirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmUnshare = false }) {
                    Text(text = stringResource(ResStrings.dictionaryDetailsScreenDeleteDialogCancel))
                }
            },
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = sharing.isShared,
                    enabled = sharing.canToggle,
                    role = Role.Switch,
                    onValueChange = { shared -> if (shared) onSharedChange(true) else confirmUnshare = true },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(ResStrings.dictionarySharingTitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        when {
                            sharing.isShared && sharing.isLastShared -> ResStrings.dictionarySharingLastShared
                            sharing.isShared -> ResStrings.dictionarySharingOn
                            else -> ResStrings.dictionarySharingOff
                        }
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // The row handles the toggle, so the switch itself takes no clicks.
            Switch(checked = sharing.isShared, onCheckedChange = null, enabled = sharing.canToggle)
        }
    }
}

@PreviewLightDark
@Composable
private fun DictionarySharingSwitchPreview() {
    VerborumTheme {
        DictionarySharingSwitch(
            sharing = DictionarySharingUi(isShared = true, isLastShared = true),
            dictionaryName = "German Basics",
            onSharedChange = {},
        )
    }
}
