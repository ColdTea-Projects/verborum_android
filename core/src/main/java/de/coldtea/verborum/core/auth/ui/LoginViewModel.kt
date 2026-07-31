package de.coldtea.verborum.core.auth.ui

import android.content.Intent
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.auth.domain.model.LoginOutcome
import de.coldtea.verborum.core.auth.ui.model.LoginState
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // A hosted sign-up that ends in Keycloak's "verify your email" page never redirects back, so
    // the result is indistinguishable from a cancel — remembering which flow was launched is the
    // only way to tell the user to check their inbox rather than silently returning to Idle.
    private var signUpLaunched = false

    /** Built lazily on tap so the AppAuth browser intent is fresh for each attempt. */
    fun loginIntent(): Intent {
        signUpLaunched = false
        return authService.loginIntent()
    }

    fun signUpIntent(): Intent {
        signUpLaunched = true
        return authService.signUpIntent()
    }

    /** Receives the AppAuth redirect result and drives the token exchange. */
    fun onAuthResult(data: Intent?) {
        if (data == null) {
            // Cancelled, dismissed, or left on the "verify your email" page.
            _loginState.value =
                if (signUpLaunched) LoginState.AwaitingEmailVerification else LoginState.Idle
            return
        }
        _loginState.value = LoginState.Authenticating
        viewModelScope.launch(exceptionHandler) {
            // On success the shell swaps this screen out once isLoggedIn flips; reset either way.
            _loginState.value = when (authService.completeLogin(data)) {
                LoginOutcome.Success -> {
                    signUpLaunched = false
                    LoginState.Idle
                }
                LoginOutcome.EmailNotVerified -> LoginState.AwaitingEmailVerification
                LoginOutcome.Failed -> LoginState.Failed
            }
        }
    }
}
