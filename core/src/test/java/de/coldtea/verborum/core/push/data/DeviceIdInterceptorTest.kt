package de.coldtea.verborum.core.push.data

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.BuildConfig
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceIdInterceptorTest : BaseTest() {

    @MockK
    private lateinit var deviceIdStore: DeviceIdStore

    private val dictionaryApi = BuildConfig.ROOT_URL_VERBORUM_API

    private lateinit var interceptor: DeviceIdInterceptor

    override fun setUp() {
        super.setUp()
        every { deviceIdStore.deviceId() } returns DEVICE_ID
        interceptor = DeviceIdInterceptor(deviceIdStore)
    }

    /** Runs [request] through the interceptor and returns the request that went down the chain. */
    private fun proceeded(request: Request): Request {
        val sent = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(capture(sent)) } returns mockk<Response>()
        interceptor.intercept(chain)
        return sent.captured
    }

    private fun write(url: String, method: String = "POST"): Request =
        Request.Builder().url(url).method(method, "{}".toRequestBody()).build()

    @Test
    fun `a write to ms_dictionary carries the device id`() {
        val sent = proceeded(write("${dictionaryApi}dictionaries/"))

        assertEquals(DEVICE_ID, sent.header("X-Device-Id"))
    }

    @Test
    fun `PUT and DELETE count as writes too`() {
        assertEquals(DEVICE_ID, proceeded(write("${dictionaryApi}words", "PUT")).header("X-Device-Id"))
        assertEquals(
            DEVICE_ID,
            proceeded(Request.Builder().url("${dictionaryApi}words/w1").delete().build()).header("X-Device-Id"),
        )
    }

    @Test
    fun `a read carries no device id`() {
        val sent = proceeded(Request.Builder().url("${dictionaryApi}dictionaries/u1").get().build())

        assertNull(sent.header("X-Device-Id"))
    }

    @Test
    fun `a write to another service carries no device id`() {
        // ms_user, ms_marketplace and ms_notification have no use for it
        val sent = proceeded(write("${BuildConfig.ROOT_URL_VERBORUM_USER_API}users/"))

        assertNull(sent.header("X-Device-Id"))
    }

    @Test
    fun `an explicit header is left alone`() {
        val request = write("${dictionaryApi}dictionaries/").newBuilder().header("X-Device-Id", "explicit").build()

        assertEquals("explicit", proceeded(request).header("X-Device-Id"))
    }

    private companion object {
        const val DEVICE_ID = "3f2c1b8e-6a54-4f0e-9d2a-1b7c5e8a9f10"
    }
}
