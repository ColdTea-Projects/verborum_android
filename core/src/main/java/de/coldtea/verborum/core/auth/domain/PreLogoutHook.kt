package de.coldtea.verborum.core.auth.domain

/**
 * Work a feature module must do **while the session is still valid** — before the tokens are cleared.
 *
 * The counterpart to [PostLogoutHook], which by design runs after the tokens are gone and may not touch
 * the network. A step that needs the bearer token (telling a server to forget this device) can only
 * live here. [AuthService] runs each hook best-effort with a short timeout, because logout can happen
 * offline and must never wait on a slow server.
 */
interface PreLogoutHook {

    suspend fun onLoggingOut()
}
