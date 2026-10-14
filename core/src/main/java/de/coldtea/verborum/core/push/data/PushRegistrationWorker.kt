package de.coldtea.verborum.core.push.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.coldtea.verborum.core.push.domain.PushRegistrationResult
import de.coldtea.verborum.core.push.domain.PushRegistrationService

/** Runs one registration attempt off the main thread; a transient failure is retried with backoff. */
@HiltWorker
class PushRegistrationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pushRegistrationService: PushRegistrationService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when (pushRegistrationService.register()) {
        PushRegistrationResult.Retry -> Result.retry()
        // Registered, Skipped and Rejected are all final for this run: the next launch or a rotated
        // token asks again, so there is nothing to keep retrying
        else -> Result.success()
    }
}
