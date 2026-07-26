package de.coldtea.verborum.core.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration test of the real OkHttp auth stack — [AuthInterceptor] attaching the bearer and
 * [TokenAuthenticator] refreshing once on a 401 — driven end to end against a [MockWebServer]. The
 * token store is mocked (its EncryptedSharedPreferences needs Android); everything else is real
 * OkHttp wiring.
 */
class TokenAuthenticatorIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: AuthTokenStore
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        val config = mockk<AuthConfig> {
            every { tokenEndpoint } returns server.url("/token").toString()
            every { clientId } returns "verborum-app"
        }
        tokenStore = mockk(relaxUnitFun = true) {
            every { currentAccessToken() } returns "old-access"
            every { currentRefreshToken() } returns "refresh-token"
        }

        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenAuthenticator(config, tokenStore))
            .build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun get(): okhttp3.Response =
        client.newCall(Request.Builder().url(server.url("/probe")).build()).execute()

    private fun tokenResponse() = MockResponse()
        .setResponseCode(200)
        .setBody("""{"access_token":"new-access","refresh_token":"new-refresh"}""")

    @Test
    fun `refreshes once on 401 and retries with the new bearer`() {
        server.enqueue(MockResponse().setResponseCode(401)) // original call
        server.enqueue(tokenResponse())                     // refresh
        server.enqueue(MockResponse().setResponseCode(200)) // retry

        val response = get()
        assertEquals(200, response.code)
        response.close()

        val original = server.takeRequest()
        assertEquals("/probe", original.path)
        assertEquals("Bearer old-access", original.getHeader("Authorization"))

        val refresh = server.takeRequest()
        assertEquals("/token", refresh.path)
        assertEquals("POST", refresh.method)
        val body = refresh.body.readUtf8()
        assertTrue(body.contains("grant_type=refresh_token"))
        assertTrue(body.contains("refresh_token=refresh-token"))
        assertTrue(body.contains("client_id=verborum-app"))

        val retry = server.takeRequest()
        assertEquals("/probe", retry.path)
        assertEquals("Bearer new-access", retry.getHeader("Authorization"))

        verify(exactly = 1) { tokenStore.updateAccessToken("new-access", "new-refresh") }
    }

    @Test
    fun `clears the session and gives up when the refresh is rejected`() {
        server.enqueue(MockResponse().setResponseCode(401)) // original call
        server.enqueue(MockResponse().setResponseCode(400)) // refresh rejected

        val response = get()
        assertEquals(401, response.code)
        response.close()

        assertEquals(2, server.requestCount) // no retry
        verify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `does not refresh more than once for a single call`() {
        server.enqueue(MockResponse().setResponseCode(401)) // original call
        server.enqueue(tokenResponse())                     // refresh (succeeds once)
        server.enqueue(MockResponse().setResponseCode(401)) // retry still 401

        val response = get()
        assertEquals(401, response.code)
        response.close()

        // probe, token, probe — then it gives up rather than refreshing again.
        assertEquals(3, server.requestCount)
        verify(exactly = 1) { tokenStore.updateAccessToken(any(), any()) }
    }
}
