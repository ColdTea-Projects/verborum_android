package de.coldtea.verborum.bibliotheca.onboarding.ui

import de.coldtea.verborum.bibliotheca.onboarding.domain.OnboardingService
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WelcomeViewModelTest : BaseTest() {

    @MockK(relaxed = true)
    private lateinit var onboardingService: OnboardingService

    private lateinit var viewModel: WelcomeViewModel

    override fun setUp() {
        super.setUp()
        viewModel = WelcomeViewModel(onboardingService)
    }

    @Test
    fun `completeOnboarding delegates to the service`() = runTest {
        viewModel.completeOnboarding()

        verify(exactly = 1) { onboardingService.completeOnboarding() }
    }
}
