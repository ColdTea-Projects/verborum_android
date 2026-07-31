package de.coldtea.verborum.core.auth.ui.model

/**
 * Login screen state. Success is not a variant here — it is signalled by
 * [de.coldtea.verborum.core.auth.AuthTokenStore.isLoggedIn] flipping true, which the app shell
 * watches to swap the login wall for the main graph.
 */
sealed class LoginState {
    data object Idle : LoginState()
    data object Authenticating : LoginState()
    data object Failed : LoginState()

    /**
     * A hosted sign-up finished but the account cannot get tokens until its email is confirmed —
     * the screen tells the user to open the verification mail and then sign in.
     */
    data object AwaitingEmailVerification : LoginState()
}
