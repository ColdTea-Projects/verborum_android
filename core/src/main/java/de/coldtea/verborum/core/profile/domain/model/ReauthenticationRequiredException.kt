package de.coldtea.verborum.core.profile.domain.model

/**
 * The backend refused the account deletion because the login is older than 5 minutes (403
 * `ReauthenticationRequiredException`, SEC-13). It means "sign in again", never "failed" — and it is
 * never retried on its own, because a refresh cannot make a login recent.
 */
class ReauthenticationRequiredException : Exception()
