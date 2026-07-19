package de.coldtea.verborum.bibliotheca.common.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.coldtea.verborum.bibliotheca.common.domain.SyncService

/**
 * Runs a full sync off the main thread. The trigger is deliberately decoupled from the work:
 * [de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler] enqueues this worker periodically
 * today and on demand (the seam a future push-notification handler will call), but the worker only
 * ever delegates to [SyncService].
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncService: SyncService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // SyncService swallows and logs its own failures per phase, so a run always completes;
        // the next scheduled (or push-triggered) run retries anything that did not go through.
        syncService.syncDictionaries()
        return Result.success()
    }
}
