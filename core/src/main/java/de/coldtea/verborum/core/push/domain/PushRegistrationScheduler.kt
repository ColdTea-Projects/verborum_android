package de.coldtea.verborum.core.push.domain

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.verborum.core.push.data.PushRegistrationWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides *when* the install's token is (re)registered. WorkManager gives the retry the registration
 * needs when the phone is offline, and unique work with REPLACE keeps it to one request however many
 * triggers fire together (login, app launch, a rotated token). The worker reads the current token
 * itself, so a newer request always supersedes an older one.
 */
@Singleton
class PushRegistrationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Safe to call at any time and from anywhere; a signed-out app simply does nothing. */
    fun requestRegistration() {
        val request = OneTimeWorkRequestBuilder<PushRegistrationWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val WORK_NAME = "verborum-push-registration"
        const val BACKOFF_SECONDS = 30L
    }
}
