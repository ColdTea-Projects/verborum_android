package de.coldtea.verborum.bibliotheca.push.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.push.domain.PushMessageHandler
import javax.inject.Inject

/**
 * Bibliotheca's share of push: a `SYNC` message means another device of this account changed a
 * dictionary, its words or its tags, so reconcile. Only schedules — the FCM callback must stay short.
 * `REFRESH` (someone imported or rated a published dictionary) has nothing cached here to refresh.
 */
class SyncPushMessageHandler @Inject constructor(
    private val syncScheduler: SyncScheduler,
) : PushMessageHandler {

    override fun onSyncRequested() = syncScheduler.requestSyncAfterRemoteChange()
}
