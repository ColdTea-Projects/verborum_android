package de.coldtea.verborum.core.auth.ui

import android.content.Intent
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.auth.domain.AuthService
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

    /** Built lazily on tap so the AppAuth browser intent is fresh for each attempt. */
    fun loginIntent(): Intent = authService.loginIntent()
    fun signUpIntent(): Intent = authService.signUpIntent()

    /** Receives the AppAuth redirect result and drives the token exchange. */
    fun onAuthResult(data: Intent?) {
        if (data == null) {
            // Cancelled or dismissed before the browser returned anything.
            _loginState.value = LoginState.Idle
            return
        }
        _loginState.value = LoginState.Authenticating
        viewModelScope.launch(exceptionHandler) {
            val success = authService.completeLogin(data)
            // On success the shell swaps this screen out once isLoggedIn flips; reset either way.
            _loginState.value = if (success) LoginState.Idle else LoginState.Failed
        }
    }
}
