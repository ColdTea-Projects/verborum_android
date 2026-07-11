package de.coldtea.verborum.bibliotheca.onboarding.domain

import de.coldtea.verborum.bibliotheca.onboarding.data.OnboardingRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingServiceTest : BaseTest() {

    @MockK(relaxed = true)
    private lateinit var onboardingRepository: OnboardingRepository

    private lateinit var onboardingService: OnboardingService

    override fun setUp() {
        super.setUp()
        onboardingService = OnboardingService(onboardingRepository)
    }

    @Test
    fun `isOnboardingCompleted reflects the stored flag`() {
        every { onboardingRepository.isOnboardingCompleted() } returns false
        assertFalse(onboardingService.isOnboardingCompleted())

        every { onboardingRepository.isOnboardingCompleted() } returns true
        assertTrue(onboardingService.isOnboardingCompleted())
    }

    @Test
    fun `completeOnboarding persists the flag`() {
        onboardingService.completeOnboarding()

        verify(exactly = 1) { onboardingRepository.setOnboardingCompleted() }
    }
}
