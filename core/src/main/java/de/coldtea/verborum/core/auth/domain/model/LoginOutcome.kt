package de.coldtea.verborum.core.auth.domain.model

/**
 * What came back from an AppAuth redirect. [EmailNotVerified] is its own outcome because the
 * backend now requires a verified address after a hosted sign-up: the user authenticated, but the
 * session must not be established and the screen has to explain *why* rather than show a generic
 * failure.
 */
sealed class LoginOutcome {
    data object Success : LoginOutcome()
    data object EmailNotVerified : LoginOutcome()
    data object Failed : LoginOutcome()
}
