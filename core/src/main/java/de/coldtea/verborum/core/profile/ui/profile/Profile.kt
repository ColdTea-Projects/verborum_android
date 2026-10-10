package de.coldtea.verborum.core.profile.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.core.profile.ui.profile.composables.LeaveForumDialog
import de.coldtea.verborum.core.profile.ui.profile.composables.MarketplaceTermsSection
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileForm
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileState
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages
import de.coldtea.verborum.core.ui.components.ScreenError
import de.coldtea.verborum.core.utils.ResStrings

/**
 * The user's profile (guide §6.2): the email (read-only), the public display name, and the Forum
 * terms. Accepting the terms with a name is what makes the user a Forum member.
 */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    RegisterTopBar(title = stringResource(ResStrings.optionsProfile))

    // Update failures surface here; the form keeps the user's drafts so they can retry.
    ShowSnackbarMessages(viewModel.snackbarMessages)

    when (val current = state) {
        ProfileState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is ProfileState.Success -> ProfileContent(
            form = current.form,
            onDisplayNameChange = viewModel::onDisplayNameChange,
            onAgreementChange = viewModel::onAgreementChange,
            onUpdate = viewModel::update,
            onConfirmLeave = viewModel::confirmLeaveForum,
            onDismissLeave = viewModel::dismissLeaveConfirmation,
        )

        ProfileState.Failed -> ScreenError(onRetry = viewModel::refresh)
    }
}

@Composable
private fun ProfileContent(
    form: ProfileForm,
    onDisplayNameChange: (String) -> Unit,
    onAgreementChange: (Boolean) -> Unit,
    onUpdate: () -> Unit,
    onConfirmLeave: () -> Unit,
    onDismissLeave: () -> Unit,
) {
    if (form.isLeaveConfirmationShown) {
        LeaveForumDialog(onConfirm = onConfirmLeave, onDismiss = onDismissLeave)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        // Everything above the Update button scrolls; the button stays pinned to the bottom.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = form.email,
                onValueChange = {},
                // Read-only rather than disabled: the address stays fully legible.
                readOnly = true,
                label = { Text(text = stringResource(ResStrings.profileEmailLabel)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = form.displayName,
                onValueChange = onDisplayNameChange,
                enabled = !form.isSaving,
                label = { Text(text = stringResource(ResStrings.profileDisplayNameLabel)) },
                singleLine = true,
                isError = form.isDisplayNameMissing,
                supportingText = {
                    Text(
                        text = stringResource(
                            if (form.isDisplayNameMissing) ResStrings.profileDisplayNameRequired
                            else ResStrings.profileDisplayNameSupporting
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            MarketplaceTermsSection(
                accepted = form.agreementAccepted,
                onAcceptedChange = onAgreementChange,
                enabled = !form.isSaving,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onUpdate,
            enabled = form.canUpdate,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (form.isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(
                    text = stringResource(ResStrings.profileUpdate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@PreviewLightDark
@Composable
private fun ProfileContentPreview() {
    VerborumTheme {
        ProfileContent(
            form = ProfileForm(
                email = "anna@example.com",
                savedDisplayName = "Anna",
                savedAgreementAccepted = false,
                displayName = "Anna Bauer",
                agreementAccepted = true,
            ),
            onDisplayNameChange = {},
            onAgreementChange = {},
            onUpdate = {},
            onConfirmLeave = {},
            onDismissLeave = {},
        )
    }
}
