package de.coldtea.verborum.bibliotheca.auth.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.profile.domain.MarketplaceMembershipHook
import javax.inject.Inject

/**
 * Joining shares all of the user's dictionaries and leaving makes them all private — on the
 * server. Re-reads them shortly after, so the sharing switches show what the server now holds.
 */
class BibliothecaMembershipHook @Inject constructor(
    private val syncScheduler: SyncScheduler,
) : MarketplaceMembershipHook {

    override suspend fun onMembershipChanged(isMember: Boolean) =
        syncScheduler.requestSyncAfterMembershipChange()
}
