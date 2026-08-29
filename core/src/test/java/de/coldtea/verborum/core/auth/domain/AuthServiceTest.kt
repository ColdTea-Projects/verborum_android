package de.coldtea.verborum.core.auth.domain

import android.content.Intent
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.auth.JwtDecoder
import de.coldtea.verborum.core.auth.domain.model.LoginOutcome
import de.coldtea.verborum.core.auth.domain.model.TokenBundle
import de.coldtea.verborum.core.auth.domain.usecase.EnsureUserProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthServiceTest : BaseTest() {

    @MockK
    private lateinit var authManager: AuthManager

    @MockK
    private lateinit var tokenStore: AuthTokenStore

    @MockK
    private lateinit var ensureUserProfileUseCase: EnsureUserProfileUseCase

    @MockK
    private lateinit var firstHook: PostLoginHook

    @MockK
    private lateinit var secondHook: PostLoginHook

    @MockK
    private lateinit var firstLogoutHook: PostLogoutHook

    @MockK
    private lateinit var secondLogoutHook: PostLogoutHook

    private val redirectData = mockk<Intent>()

    private lateinit var authService: AuthService

    override fun setUp() {
        super.setUp()
        // JwtDecoder is an object over android.util.Base64, which is not available on the JVM.
        mockkObject(JwtDecoder)
        every { JwtDecoder.subject(ACCESS_TOKEN) } returns SUBJECT
        every { JwtDecoder.email(ID_TOKEN) } returns EMAIL
        every { JwtDecoder.displayName(ID_TOKEN) } returns DISPLAY_NAME
        every { JwtDecoder.emailVerified(ID_TOKEN) } returns true
        coEvery { authManager.exchangeCode(redirectData) } returns tokenBundle

        // LinkedHashSet so hook order is deterministic — the production set is Dagger-built.
        authService = AuthService(
            authManager = authManager,
            tokenStore = tokenStore,
            ensureUserProfileUseCase = ensureUserProfileUseCase,
            postLoginHooks = linkedSetOf(firstHook, secondHook),
            postLogoutHooks = linkedSetOf(firstLogoutHook, secondLogoutHook),
        )
    }

    // region completeLogin — happy path
    @Test
    fun `completeLogin persists tokens before ensuring the profile and running the hooks`() =
        runTest {
            assertEquals(LoginOutcome.Success, authService.completeLogin(redirectData))

            coVerifyOrder {
                tokenStore.saveTokens(
                    accessToken = ACCESS_TOKEN,
                    refreshToken = REFRESH_TOKEN,
                    subject = SUBJECT,
                )
                ensureUserProfileUseCase.invoke(
                    subject = SUBJECT,
                    email = EMAIL,
                    displayName = DISPLAY_NAME,
                )
                firstHook.onLoginCompleted(SUBJECT)
                secondHook.onLoginCompleted(SUBJECT)
            }
        }

    @Test
    fun `completeLogin runs every registered hook with the signed-in subject`() = runTest {
        authService.completeLogin(redirectData)

        coVerify(exactly = 1) { firstHook.onLoginCompleted(SUBJECT) }
        coVerify(exactly = 1) { secondHook.onLoginCompleted(SUBJECT) }
    }
    // endregion

    // region completeLogin — best-effort follow-ups
    @Test
    fun `completeLogin runs the remaining hooks when an earlier one throws`() = runTest {
        coEvery { firstHook.onLoginCompleted(SUBJECT) } throws RuntimeException("migration blew up")

        assertEquals(LoginOutcome.Success, authService.completeLogin(redirectData))

        coVerify(exactly = 1) { secondHook.onLoginCompleted(SUBJECT) }
    }

    @Test
    fun `completeLogin still succeeds and runs the hooks when profile creation throws`() = runTest {
        coEvery {
            ensureUserProfileUseCase.invoke(any(), any(), any())
        } throws RuntimeException("ms_user unreachable")

        assertEquals(LoginOutcome.Success, authService.completeLogin(redirectData))

        coVerify(exactly = 1) { firstHook.onLoginCompleted(SUBJECT) }
    }
    // endregion

    // region completeLogin — failure paths
    @Test
    fun `completeLogin fails without touching the session when the code exchange fails`() =
        runTest {
            coEvery { authManager.exchangeCode(redirectData) } returns null

            assertEquals(LoginOutcome.Failed, authService.completeLogin(redirectData))

            verify(exactly = 0) { tokenStore.saveTokens(any(), any(), any()) }
            coVerify(exactly = 0) { firstHook.onLoginCompleted(any()) }
        }

    @Test
    fun `completeLogin fails without touching the session when the token carries no subject`() =
        runTest {
            every { JwtDecoder.subject(ACCESS_TOKEN) } returns null

            assertEquals(LoginOutcome.Failed, authService.completeLogin(redirectData))

            verify(exactly = 0) { tokenStore.saveTokens(any(), any(), any()) }
            coVerify(exactly = 0) { firstHook.onLoginCompleted(any()) }
        }
    @Test
    fun `completeLogin reports an unverified email without establishing the session`() = runTest {
        every { JwtDecoder.emailVerified(ID_TOKEN) } returns false

        assertEquals(LoginOutcome.EmailNotVerified, authService.completeLogin(redirectData))

        verify(exactly = 0) { tokenStore.saveTokens(any(), any(), any()) }
        coVerify(exactly = 0) { firstHook.onLoginCompleted(any()) }
    }
    // endregion

    // region logout
    @Test
    fun `logout ends the Keycloak session before clearing the local tokens`() = runTest {
        every { tokenStore.currentRefreshToken() } returns REFRESH_TOKEN

        authService.logout()

        verify {
            authManager.endSession(REFRESH_TOKEN)
            tokenStore.clear()
        }
    }

    @Test
    fun `logout clears the local tokens even when the back-channel call fails`() = runTest {
        every { tokenStore.currentRefreshToken() } returns REFRESH_TOKEN
        every { authManager.endSession(any()) } throws RuntimeException("Keycloak unreachable")

        authService.logout()

        verify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `logout runs every post-logout hook after clearing the tokens`() = runTest {
        every { tokenStore.currentRefreshToken() } returns REFRESH_TOKEN

        authService.logout()

        // Tokens go first so a hook can never run against a half-live session.
        coVerifyOrder {
            tokenStore.clear()
            firstLogoutHook.onLoggedOut()
            secondLogoutHook.onLoggedOut()
        }
    }

    @Test
    fun `logout runs the remaining post-logout hooks when an earlier one throws`() = runTest {
        every { tokenStore.currentRefreshToken() } returns REFRESH_TOKEN
        coEvery { firstLogoutHook.onLoggedOut() } throws RuntimeException("wipe blew up")

        authService.logout()

        coVerify(exactly = 1) { secondLogoutHook.onLoggedOut() }
    }
    // endregion

    private companion object {
        const val ACCESS_TOKEN = "access-token"
        const val REFRESH_TOKEN = "refresh-token"
        const val ID_TOKEN = "id-token"
        const val SUBJECT = "e7c1f0e2-subject"
        const val EMAIL = "yasar@example.com"
        const val DISPLAY_NAME = "Yasar"

        val tokenBundle = TokenBundle(
            accessToken = ACCESS_TOKEN,
            refreshToken = REFRESH_TOKEN,
            idToken = ID_TOKEN,
        )
    }
}
