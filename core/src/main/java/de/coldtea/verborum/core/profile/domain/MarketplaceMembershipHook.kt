package de.coldtea.verborum.core.profile.domain

/**
 * Work a feature module needs once the user joined or left the Forum.
 *
 * Joining makes every one of the user's dictionaries public on the server, leaving makes them all
 * private (marketplace guide §4.6) — so bibliotheca must re-read them. Core cannot depend on it,
 * so it binds an implementation `@IntoSet`, like the login/logout hooks.
 *
 * Best-effort and quick: it runs inside the profile update the user is waiting for, so heavy work
 * is to be scheduled, not done here.
 */
interface MarketplaceMembershipHook {

    suspend fun onMembershipChanged(isMember: Boolean)
}
