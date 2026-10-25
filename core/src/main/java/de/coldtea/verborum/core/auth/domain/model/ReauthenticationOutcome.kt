package de.coldtea.verborum.core.auth.domain.model

/**
 * What came back from the sign-in that account deletion asks for (`docs/account-deletion-guide.md`
 * §3, step B and C). [DifferentAccount] is its own outcome: the person signed in as someone else, so
 * nothing may be deleted and the session on file stays as it was.
 */
sealed class ReauthenticationOutcome {
    data object Success : ReauthenticationOutcome()
    data object DifferentAccount : ReauthenticationOutcome()
    data object Failed : ReauthenticationOutcome()
}
