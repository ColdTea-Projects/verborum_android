package de.coldtea.verborum.core.push.domain

/**
 * What a feature module does when a silent push arrives. Push lives in `core`, which must never
 * depend on a feature module, so each module contributes its handler `@IntoSet` — the same shape as
 * the login and logout hooks.
 *
 * Handlers run on the FCM service's short-lived callback: schedule work, never do it inline. They
 * must be idempotent — a push is a hint and can arrive twice or not at all.
 */
interface PushMessageHandler {

    /** `SYNC`: the same account changed a dictionary, its words or tags on another device. */
    fun onSyncRequested() = Unit

    /** `REFRESH`: someone imported or rated a dictionary this user published. */
    fun onRefreshRequested() = Unit
}
