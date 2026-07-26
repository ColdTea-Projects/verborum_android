package de.coldtea.verborum.bibliotheca.auth.domain

import de.coldtea.verborum.bibliotheca.auth.domain.usecase.MigrateGuestDataUseCase
import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.auth.domain.PostLoginHook
import javax.inject.Inject

/**
 * Bibliotheca's share of the login lifecycle: re-own the guest dictionaries under the signed-in
 * subject, then reconcile fully so the migrated rows go up and any server-side data comes down.
 *
 * Order matters — migrating first means the sync uploads rows that already carry the real owner.
 */
class BibliothecaPostLoginHook @Inject constructor(
    private val migrateGuestDataUseCase: MigrateGuestDataUseCase,
    private val syncScheduler: SyncScheduler,
) : PostLoginHook {

    override suspend fun onLoginCompleted(subject: String) {
        migrateGuestDataUseCase.invoke(subject)
        syncScheduler.requestImmediateSync(uploadOnly = false)
    }
}
