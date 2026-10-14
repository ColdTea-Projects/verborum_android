package de.coldtea.verborum.bibliotheca.push.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Test

class SyncPushMessageHandlerTest : BaseTest() {

    @MockK(relaxed = true)
    private lateinit var syncScheduler: SyncScheduler

    private lateinit var handler: SyncPushMessageHandler

    override fun setUp() {
        super.setUp()
        handler = SyncPushMessageHandler(syncScheduler)
    }

    @Test
    fun `a SYNC push schedules a full reconcile after the debounce`() {
        handler.onSyncRequested()

        verify(exactly = 1) { syncScheduler.requestSyncAfterRemoteChange() }
    }

    @Test
    fun `a REFRESH push does nothing here — there is nothing cached to refresh`() {
        handler.onRefreshRequested()

        verify(exactly = 0) { syncScheduler.requestSyncAfterRemoteChange() }
        verify(exactly = 0) { syncScheduler.requestImmediateSync(any()) }
    }
}
