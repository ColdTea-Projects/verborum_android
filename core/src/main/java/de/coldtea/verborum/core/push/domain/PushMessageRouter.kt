package de.coldtea.verborum.core.push.domain

import de.coldtea.verborum.core.auth.AuthTokenStore
import javax.inject.Inject

/**
 * Turns an FCM data payload into a call on the feature handlers (docs/push-notifications-client-guide.md
 * §4). Messages carry ids only and are never the source of truth, so anything unexpected — a signed-out
 * app, an unknown `type`, a handler that throws — is ignored rather than surfaced.
 */
class PushMessageRouter @Inject constructor(
    private val handlers: Set<@JvmSuppressWildcards PushMessageHandler>,
    private val tokenStore: AuthTokenStore,
) {

    fun route(data: Map<String, String>) {
        // A push for a previous account must not wake the next one's sync
        if (tokenStore.currentUserId() == null) return

        when (data[KEY_TYPE]) {
            TYPE_SYNC -> handlers.forEach { runCatching { it.onSyncRequested() } }
            TYPE_REFRESH -> handlers.forEach { runCatching { it.onRefreshRequested() } }
            // Unknown types are ignored on purpose: the schema evolves server-side first
            else -> Unit
        }
    }

    private companion object {
        const val KEY_TYPE = "type"
        const val TYPE_SYNC = "SYNC"
        const val TYPE_REFRESH = "REFRESH"
    }
}
