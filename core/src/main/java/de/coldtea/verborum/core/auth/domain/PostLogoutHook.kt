package de.coldtea.verborum.core.auth.domain

/**
 * Work a feature module needs to run once a logout has torn down the session.
 *
 * The counterpart to [PostLoginHook]: auth lives in `core`, which must never depend on a feature
 * module, yet logout still has to trigger feature-owned cleanup — above all wiping the local
 * database, so the next account signing in on this device can never see, upload, or reconcile away
 * the previous user's rows. Feature modules bind their own implementation `@IntoSet`, and
 * [AuthService] runs them after the tokens are cleared.
 *
 * Implementations must be idempotent and must not require network — logout can happen offline.
 */
interface PostLogoutHook {

    suspend fun onLoggedOut()
}
