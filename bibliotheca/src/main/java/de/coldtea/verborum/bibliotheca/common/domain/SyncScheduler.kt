package de.coldtea.verborum.bibliotheca.common.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.verborum.bibliotheca.common.data.sync.SyncWorker
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single seam that decides *when* a background sync runs. Everything downstream
 * ([SyncWorker] → [SyncService]) is trigger-agnostic, so the mechanism here can be replaced
 * without touching the sync logic.
 *
 * Today: a network-constrained periodic worker is the background refresh. When push notifications
 * arrive, the message handler only needs to call [requestImmediateSync] and the periodic cadence
 * can be relaxed to a rare safety net — no other code changes.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Guards against registering the reconnect callback more than once (see syncOnReconnect).
    private val reconnectCallbackRegistered = AtomicBoolean(false)

    /** Idempotent: registers the recurring background sync once, keeping any existing schedule. */
    fun ensurePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /**
     * Runs a sync whenever a network becomes available, so changes made offline (or edited on
     * another device) reconcile promptly on reconnect instead of waiting for the periodic cadence.
     *
     * [requestImmediateSync] itself carries a CONNECTED constraint and coalesces via unique-work
     * KEEP, so firing on every `onAvailable` — including the echo for a network already present at
     * launch, or a Wi‑Fi↔cellular handoff — is safe: redundant runs collapse, and an unchanged
     * sync writes nothing. Registered once for the process lifetime and intentionally never
     * unregistered (it should live as long as the app).
     */
    fun syncOnReconnect() {
        if (!reconnectCallbackRegistered.compareAndSet(false, true)) return
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return

        // registerNetworkCallback (not registerDefaultNetworkCallback, which is API 24) keeps this
        // compatible with minSdk 23.
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        manager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    requestImmediateSync()
                }
            },
        )
    }

    /**
     * Fires a one-off sync as soon as the network allows. This is the hook a push-notification
     * handler will call; concurrent requests coalesce onto the single in-flight run.
     */
    fun requestImmediateSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val PERIODIC_WORK_NAME = "verborum-periodic-sync"
        const val IMMEDIATE_WORK_NAME = "verborum-immediate-sync"
        const val PERIODIC_INTERVAL_HOURS = 6L
    }
}
