package de.coldtea.verborum.core.auth.domain

/**
 * Work a feature module needs to run once a login has established a session.
 *
 * Auth lives in `core`, which must never depend on a feature module — but the login lifecycle still
 * has to trigger feature-owned work (guest-data migration, kicking a sync). Feature modules bind
 * their own implementation `@IntoSet`, and [AuthService] runs them after the tokens are persisted.
 *
 * Implementations must be idempotent and best-effort: a failing hook must not strand a validly
 * authenticated user on the login screen.
 */
interface PostLoginHook {

    /** @param subject the signed-in user's JWT subject — the owner id stamped on local rows. */
    suspend fun onLoginCompleted(subject: String)
}
