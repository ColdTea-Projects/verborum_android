package de.coldtea.verborum.core.profile.ui.deleteaccount

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.auth.domain.model.ReauthenticationOutcome
import de.coldtea.verborum.core.profile.domain.AccountDeletionService
import de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException
import de.coldtea.verborum.core.profile.ui.deleteaccount.model.DeleteAccountState
import de.coldtea.verborum.core.profile.ui.deleteaccount.model.DeleteAccountStep
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.core.utils.ResStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The delete-account flow of the profile page (`docs/account-deletion-guide.md` §3): warning with a
 * checkbox → sign in again (`max_age=0`) → same-account check → final dialog → `DELETE`. No step
 * retries by itself: a 403 that asks for a new sign-in sends the person back to the warning.
 *
 * On success nothing navigates: the wipe clears the tokens, and the app shell swaps in the login wall.
 */
@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val authService: AuthService,
    private val accountDeletionService: AccountDeletionService,
) : BaseViewModel() {

    private val _state = MutableStateFlow(DeleteAccountState())
    val state: StateFlow<DeleteAccountState> = _state.asStateFlow()

    fun onDeleteClick() = update { if (it.step == DeleteAccountStep.Idle) DeleteAccountState(DeleteAccountStep.Warning) else it }

    fun onAcknowledgedChange(acknowledged: Boolean) = update {
        if (it.step == DeleteAccountStep.Warning) it.copy(acknowledged = acknowledged) else it
    }

    /** Cancel on the warning or on the final dialog: back to the profile page, still signed in. */
    fun onDismiss() = update {
        if (it.step == DeleteAccountStep.Warning || it.step == DeleteAccountStep.Confirm) DeleteAccountState() else it
    }

    /**
     * The sign-in page for step B, or null when the warning was not acknowledged. Built on tap, so the
     * browser intent is fresh for each attempt.
     */
    fun reauthenticationIntent(): Intent? {
        val current = _state.value
        if (current.step != DeleteAccountStep.Warning || !current.acknowledged) return null
        _state.value = current.copy(step = DeleteAccountStep.SigningIn)
        return authService.reauthenticationIntent()
    }

    /** Receives the redirect of the sign-in. A null [data] is a cancel: nothing changed, still signed in. */
    fun onReauthenticationResult(data: Intent?) {
        if (_state.value.step != DeleteAccountStep.SigningIn) return
        if (data == null) {
            _state.value = DeleteAccountState()
            return
        }
        _state.value = _state.value.copy(step = DeleteAccountStep.Verifying)
        viewModelScope.launch(exceptionHandler) {
            when (authService.completeReauthentication(data)) {
                ReauthenticationOutcome.Success -> _state.value = DeleteAccountState(DeleteAccountStep.Confirm)
                ReauthenticationOutcome.DifferentAccount -> abort(ResStrings.deleteAccountErrorDifferentAccount)
                ReauthenticationOutcome.Failed -> abort(ResStrings.deleteAccountErrorSignIn)
            }
        }
    }

    fun onConfirmDelete() {
        if (_state.value.step != DeleteAccountStep.Confirm) return
        _state.value = DeleteAccountState(DeleteAccountStep.Deleting)
        viewModelScope.launch(exceptionHandler) {
            try {
                accountDeletionService.deleteAccount()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ReauthenticationRequiredException) {
                // Took longer than 5 minutes. The person starts the sign-in again by hand — never automatic.
                _state.value = DeleteAccountState(DeleteAccountStep.Warning, acknowledged = true)
                _snackbarMessages.emit(UiText.Resource(ResStrings.deleteAccountErrorReauth))
            } catch (e: Exception) {
                // The result is unknown (guide §8): nothing was wiped, and "Delete" tries again.
                _state.value = DeleteAccountState(DeleteAccountStep.Confirm)
                _snackbarMessages.emit(UiText.Resource(ResStrings.deleteAccountErrorUnknown))
            }
        }
    }

    private suspend fun abort(@StringRes messageRes: Int) {
        _state.value = DeleteAccountState()
        _snackbarMessages.emit(UiText.Resource(messageRes))
    }

    private fun update(transform: (DeleteAccountState) -> DeleteAccountState) = _state.update(transform)
}
