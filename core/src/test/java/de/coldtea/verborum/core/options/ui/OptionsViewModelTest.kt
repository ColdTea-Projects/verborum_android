package de.coldtea.verborum.core.options.ui

import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OptionsViewModelTest : BaseTest() {

    @MockK
    private lateinit var authService: AuthService

    private lateinit var viewModel: OptionsViewModel

    override fun setUp() {
        super.setUp()
        viewModel = OptionsViewModel(authService)
    }

    @Test
    fun `logout delegates to AuthService`() = runTest {
        viewModel.logout()

        coVerify(exactly = 1) { authService.logout() }
    }
}
