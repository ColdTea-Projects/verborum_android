package de.coldtea.verborum.core.options.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionsViewModel @Inject constructor(
    private val authService: AuthService,
) : BaseViewModel() {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    /**
     * Ends the session. No explicit navigation follows: clearing the tokens flips
     * [de.coldtea.verborum.core.auth.AuthTokenStore.isLoggedIn], and the app shell swaps the whole
     * graph for the login wall.
     */
    fun logout() {
        if (_isLoggingOut.value) return
        _isLoggingOut.value = true
        viewModelScope.launch(exceptionHandler) {
            try {
                authService.logout()
            } finally {
                _isLoggingOut.value = false
            }
        }
    }
}
