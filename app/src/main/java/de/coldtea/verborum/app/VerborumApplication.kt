package de.coldtea.verborum.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import de.coldtea.verborum.bibliotheca.common.domain.PendingUploadSyncTrigger
import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import javax.inject.Inject

@HiltAndroidApp
class VerborumApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var pendingUploadSyncTrigger: PendingUploadSyncTrigger

    // On-demand WorkManager init (the default initializer is removed in the manifest) so Hilt can
    // inject the worker factory before WorkManager reads this configuration.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncScheduler.ensurePeriodicSync()
        // Reconcile as soon as connectivity returns, rather than waiting for the periodic cadence.
        syncScheduler.syncOnReconnect()
        // Push local changes to the server within seconds of a save/edit/delete.
        pendingUploadSyncTrigger.start()
    }
}
