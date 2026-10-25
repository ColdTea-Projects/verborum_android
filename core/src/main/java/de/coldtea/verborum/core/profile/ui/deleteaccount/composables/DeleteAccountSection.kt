package de.coldtea.verborum.core.profile.ui.deleteaccount.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.profile.ui.deleteaccount.DeleteAccountViewModel
import de.coldtea.verborum.core.profile.ui.deleteaccount.model.DeleteAccountStep
import de.coldtea.verborum.core.ui.ShowSnackbarMessages
import de.coldtea.verborum.core.utils.ResStrings

/**
 * The destructive entry at the bottom of the profile page, apart from the form, and the whole flow
 * behind it (`docs/account-deletion-guide.md` §3). Deleting needs the network and a fresh sign-in, so
 * the button is off while offline and a started flow is withdrawn when the connection drops.
 */
@Composable
fun DeleteAccountSection(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onReauthenticationResult(result.data)
    }

    ShowSnackbarMessages(viewModel.snackbarMessages)

    // A warning or a final dialog cannot be completed offline, so it is withdrawn.
    LaunchedEffect(isOnline) {
        if (!isOnline) viewModel.onDismiss()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = viewModel::onDeleteClick,
            enabled = isOnline && state.step == DeleteAccountStep.Idle,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = if (isOnline) 1f else 0.38f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(ResStrings.profileDeleteAccount),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(ResStrings.profileDeleteAccountHint),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    when (state.step) {
        DeleteAccountStep.Warning -> DeleteAccountWarningDialog(
            acknowledged = state.acknowledged,
            onAcknowledgedChange = viewModel::onAcknowledgedChange,
            onContinue = { viewModel.reauthenticationIntent()?.let(launcher::launch) },
            onDismiss = viewModel::onDismiss,
        )

        DeleteAccountStep.Verifying -> DeleteAccountProgressDialog(message = null)

        DeleteAccountStep.Confirm -> DeleteAccountConfirmDialog(
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onDismiss,
        )

        DeleteAccountStep.Deleting ->
            DeleteAccountProgressDialog(message = stringResource(ResStrings.deleteAccountProgress))

        DeleteAccountStep.Idle, DeleteAccountStep.SigningIn -> Unit
    }
}
