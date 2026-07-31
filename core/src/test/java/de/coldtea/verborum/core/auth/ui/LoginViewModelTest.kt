package de.coldtea.verborum.core.auth.ui

import android.content.Intent
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.auth.domain.model.LoginOutcome
import de.coldtea.verborum.core.auth.ui.model.LoginState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginViewModelTest : BaseTest() {

    @MockK
    private lateinit var authService: AuthService

    private val redirectData = mockk<Intent>()
    private val browserIntent = mockk<Intent>()

    private lateinit var viewModel: LoginViewModel

    override fun setUp() {
        super.setUp()
        every { authService.loginIntent() } returns browserIntent
        every { authService.signUpIntent() } returns browserIntent
        viewModel = LoginViewModel(authService)
    }

    @Test
    fun `successful login returns to idle`() = runTest {
        coEvery { authService.completeLogin(redirectData) } returns LoginOutcome.Success

        viewModel.onAuthResult(redirectData)

        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }

    @Test
    fun `an unverified email shows the verification notice`() = runTest {
        coEvery { authService.completeLogin(redirectData) } returns LoginOutcome.EmailNotVerified

        viewModel.onAuthResult(redirectData)

        assertEquals(LoginState.AwaitingEmailVerification, viewModel.loginState.value)
    }

    @Test
    fun `a failed exchange shows the error`() = runTest {
        coEvery { authService.completeLogin(redirectData) } returns LoginOutcome.Failed

        viewModel.onAuthResult(redirectData)

        assertEquals(LoginState.Failed, viewModel.loginState.value)
    }

    @Test
    fun `a sign-up that never redirects back shows the verification notice`() = runTest {
        // Keycloak parks the browser on its "verify your email" page — no redirect, so the
        // launcher hands back a null result exactly like a cancel.
        viewModel.signUpIntent()

        viewModel.onAuthResult(null)

        assertEquals(LoginState.AwaitingEmailVerification, viewModel.loginState.value)
    }

    @Test
    fun `a cancelled sign-in stays idle`() = runTest {
        viewModel.loginIntent()

        viewModel.onAuthResult(null)

        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }

    @Test
    fun `a cancelled sign-in after a sign-up no longer nags about verification`() = runTest {
        viewModel.signUpIntent()
        viewModel.loginIntent()

        viewModel.onAuthResult(null)

        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }
}
