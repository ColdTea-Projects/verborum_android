package de.coldtea.verborum.bibliotheca.common.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.coldtea.verborum.bibliotheca.common.domain.SyncService

/**
 * Runs a sync off the main thread. The trigger is deliberately decoupled from the work:
 * [de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler] enqueues this worker periodically
 * and on demand, but the worker only ever delegates to [SyncService].
 *
 * [KEY_UPLOAD_ONLY] selects the phase: the change-triggered immediate sync pushes local changes
 * only (cheap, doesn't re-download the dataset every save), while periodic/reconnect runs do the
 * full upload-then-download reconcile.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncService: SyncService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // SyncService swallows and logs its own failures per phase, so a run always completes;
        // the next scheduled (or triggered) run retries anything that did not go through.
        if (inputData.getBoolean(KEY_UPLOAD_ONLY, false)) {
            syncService.uploadPendingChanges()
        } else {
            syncService.syncDictionaries()
        }
        return Result.success()
    }

    companion object {
        const val KEY_UPLOAD_ONLY = "upload_only"
    }
}
