package de.coldtea.verborum.core.push.domain

import android.content.Context
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.push.data.DeviceIdStore
import de.coldtea.verborum.core.push.data.FcmTokenSource
import de.coldtea.verborum.core.push.data.api.NotificationApi
import de.coldtea.verborum.core.push.data.api.model.DeviceRegistrationRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class PushRegistrationServiceTest : BaseTest() {

    @MockK
    private lateinit var api: NotificationApi

    @MockK
    private lateinit var deviceIdStore: DeviceIdStore

    @MockK
    private lateinit var fcmTokenSource: FcmTokenSource

    @MockK
    private lateinit var tokenStore: AuthTokenStore

    private val context = mockk<Context>(relaxed = true)

    private lateinit var service: PushRegistrationService

    override fun setUp() {
        super.setUp()
        every { tokenStore.currentUserId() } returns SUBJECT
        every { deviceIdStore.deviceId() } returns DEVICE_ID
        coEvery { fcmTokenSource.currentToken() } returns FCM_TOKEN
        coEvery { fcmTokenSource.deleteToken() } returns Unit
        coEvery { api.registerDevice(any()) } returns Response.success(Unit)
        coEvery { api.removeDevice(any()) } returns Response.success(Unit)
        service = PushRegistrationService(api, deviceIdStore, fcmTokenSource, tokenStore, context)
    }

    private fun status(code: Int): Response<Unit> = Response.error(code, "".toResponseBody())

    // region register
    @Test
    fun `register sends this install's id and token as an ANDROID device`() = runTest {
        val sent = slot<DeviceRegistrationRequest>()
        coEvery { api.registerDevice(capture(sent)) } returns Response.success(Unit)

        assertEquals(PushRegistrationResult.Registered, service.register())

        assertEquals(DEVICE_ID, sent.captured.deviceId)
        assertEquals(FCM_TOKEN, sent.captured.token)
        assertEquals("ANDROID", sent.captured.platform)
    }

    @Test
    fun `register does nothing while signed out`() = runTest {
        every { tokenStore.currentUserId() } returns null

        assertEquals(PushRegistrationResult.Skipped, service.register())

        coVerify(exactly = 0) { api.registerDevice(any()) }
        coVerify(exactly = 0) { fcmTokenSource.currentToken() }
    }

    @Test
    fun `register skips when FCM has no token yet`() = runTest {
        coEvery { fcmTokenSource.currentToken() } returns null

        assertEquals(PushRegistrationResult.Skipped, service.register())

        coVerify(exactly = 0) { api.registerDevice(any()) }
    }

    @Test
    fun `register on a 403 rotates the device id and tries once more`() = runTest {
        // The server holds this id for another account under a token this install does not have
        every { deviceIdStore.rotate() } returns NEW_DEVICE_ID
        val sent = mutableListOf<DeviceRegistrationRequest>()
        coEvery { api.registerDevice(capture(sent)) } returnsMany listOf(status(403), Response.success(Unit))

        assertEquals(PushRegistrationResult.Registered, service.register())

        assertEquals(listOf(DEVICE_ID, NEW_DEVICE_ID), sent.map { it.deviceId })
    }

    @Test
    fun `register gives up after the second 403 instead of rotating forever`() = runTest {
        every { deviceIdStore.rotate() } returns NEW_DEVICE_ID
        coEvery { api.registerDevice(any()) } returns status(403)

        assertEquals(PushRegistrationResult.Rejected, service.register())

        coVerify(exactly = 2) { api.registerDevice(any()) }
    }

    @Test
    fun `register retries on a server error and on a conflict`() = runTest {
        coEvery { api.registerDevice(any()) } returns status(503)
        assertEquals(PushRegistrationResult.Retry, service.register())

        coEvery { api.registerDevice(any()) } returns status(409)
        assertEquals(PushRegistrationResult.Retry, service.register())
    }

    @Test
    fun `register retries when the call itself fails offline`() = runTest {
        coEvery { api.registerDevice(any()) } throws IOException("offline")

        assertEquals(PushRegistrationResult.Retry, service.register())
    }

    @Test
    fun `register does not retry a validation error`() = runTest {
        coEvery { api.registerDevice(any()) } returns status(400)

        assertEquals(PushRegistrationResult.Rejected, service.register())
    }
    // endregion

    // region unregister
    @Test
    fun `unregister removes the device on the server and then the local token`() = runTest {
        service.unregister()

        io.mockk.coVerifyOrder {
            api.removeDevice(DEVICE_ID)
            fcmTokenSource.deleteToken()
        }
    }

    @Test
    fun `unregister still drops the local token when the server call fails`() = runTest {
        coEvery { api.removeDevice(any()) } throws IOException("offline")

        service.unregister()

        // The old token is dead either way, so the server drops its row on its next push
        coVerify { fcmTokenSource.deleteToken() }
    }
    // endregion

    @Test
    fun `an unavailable app version is not a reason to fail`() = runTest {
        every { context.packageManager } throws IllegalStateException("no package manager")
        val sent = slot<DeviceRegistrationRequest>()
        coEvery { api.registerDevice(capture(sent)) } returns Response.success(Unit)

        assertEquals(PushRegistrationResult.Registered, service.register())
        assertNull(sent.captured.appVersion)
    }

    private companion object {
        const val SUBJECT = "kc-subject"
        const val DEVICE_ID = "3f2c1b8e-6a54-4f0e-9d2a-1b7c5e8a9f10"
        const val NEW_DEVICE_ID = "9a8b7c6d-1111-4222-8333-444455556666"
        const val FCM_TOKEN = "fcm-token-1"
    }
}
