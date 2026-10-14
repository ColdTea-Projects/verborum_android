package de.coldtea.verborum.core.push.domain

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.AuthTokenStore
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Test

class PushMessageRouterTest : BaseTest() {

    @MockK(relaxed = true)
    private lateinit var firstHandler: PushMessageHandler

    @MockK(relaxed = true)
    private lateinit var secondHandler: PushMessageHandler

    @MockK
    private lateinit var tokenStore: AuthTokenStore

    private lateinit var router: PushMessageRouter

    override fun setUp() {
        super.setUp()
        every { tokenStore.currentUserId() } returns "kc-subject"
        router = PushMessageRouter(linkedSetOf(firstHandler, secondHandler), tokenStore)
    }

    @Test
    fun `a SYNC message reaches every handler`() {
        router.route(mapOf("type" to "SYNC", "dictionaryId" to "d1"))

        verify(exactly = 1) { firstHandler.onSyncRequested() }
        verify(exactly = 1) { secondHandler.onSyncRequested() }
        verify(exactly = 0) { firstHandler.onRefreshRequested() }
    }

    @Test
    fun `a REFRESH message reaches every handler`() {
        router.route(mapOf("type" to "REFRESH", "dictionaryId" to "d1"))

        verify(exactly = 1) { firstHandler.onRefreshRequested() }
        verify(exactly = 0) { firstHandler.onSyncRequested() }
    }

    @Test
    fun `an unknown or missing type is ignored`() {
        router.route(mapOf("type" to "SOMETHING_NEW"))
        router.route(emptyMap())

        verify(exactly = 0) { firstHandler.onSyncRequested() }
        verify(exactly = 0) { firstHandler.onRefreshRequested() }
    }

    @Test
    fun `nothing is routed while signed out`() {
        // A push meant for the previous account must not wake the next one's sync
        every { tokenStore.currentUserId() } returns null

        router.route(mapOf("type" to "SYNC"))

        verify(exactly = 0) { firstHandler.onSyncRequested() }
    }

    @Test
    fun `a handler that throws does not stop the others`() {
        every { firstHandler.onSyncRequested() } throws IllegalStateException("boom")

        router.route(mapOf("type" to "SYNC"))

        verify(exactly = 1) { secondHandler.onSyncRequested() }
    }
}
