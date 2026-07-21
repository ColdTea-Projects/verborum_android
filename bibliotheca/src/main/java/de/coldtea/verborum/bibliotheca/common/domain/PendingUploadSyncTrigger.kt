package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.common.domain.usecases.ObservePendingUploadsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns "the local database has changes" into "push them to the server now". Watches the pending
 * upload count ([ObservePendingUploadsUseCase]) and, shortly after it becomes non-zero, asks
 * [SyncScheduler] for an upload-only sync — so a save reaches the server in seconds instead of
 * waiting for the periodic cadence.
 *
 * All writes are UI-initiated (foreground), so this single observer catches every one, plus any
 * changes still pending at launch (the flow's initial emission). It runs on a process-lifetime
 * scope: even a change made just before the app is backgrounded still enqueues the WorkManager
 * job, which then survives process death.
 */
@Singleton
class PendingUploadSyncTrigger @Inject constructor(
    private val observePendingUploadsUseCase: ObservePendingUploadsUseCase,
    private val syncScheduler: SyncScheduler,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)

    // While paused (a practice screen is open), change-driven syncs are held back so a rapid run
    // of level updates does not fire an API call each; [deferredWhilePaused] remembers that at
    // least one was suppressed so [resume] can flush a single upload on close.
    private val pauseDepth = AtomicInteger(0)
    private val deferredWhilePaused = AtomicBoolean(false)

    /** Idempotent: begins observing on first call and ignores later ones. */
    fun start() {
        if (!started.compareAndSet(false, true)) return

        observePendingUploadsUseCase.invoke()
            .distinctUntilChanged()
            // Collapse a burst of edits (and the row-by-row count drop during an upload) into a
            // single request shortly after things settle, rather than one sync per write.
            .debounce(DEBOUNCE_MS)
            .filter { pending -> pending > 0 }
            .onEach {
                if (pauseDepth.get() > 0) deferredWhilePaused.set(true)
                else syncScheduler.requestImmediateSync(uploadOnly = true)
            }
            .launchIn(scope)
    }

    /**
     * Suspends change-driven immediate syncs. Practice screens call this on open so a whole session
     * of level changes reconciles once, on close, instead of hammering the API answer-by-answer.
     * The changes are still persisted locally, so they upload on [resume] (or on the next launch if
     * the process dies first, via the observer's initial emission).
     *
     * Reference-counted, so overlapping callers are balanced; [resume] must be paired with it.
     */
    fun pause() {
        pauseDepth.incrementAndGet()
    }

    /** Releases a [pause]; when the last one lifts, flushes a single upload if any was suppressed. */
    fun resume() {
        val depth = pauseDepth.updateAndGet { (it - 1).coerceAtLeast(0) }
        if (depth == 0 && deferredWhilePaused.getAndSet(false)) {
            syncScheduler.requestImmediateSync(uploadOnly = true)
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 2_000L
    }
}
