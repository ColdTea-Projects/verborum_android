package de.coldtea.verborum.bibliotheca.onboarding.domain

import de.coldtea.verborum.bibliotheca.onboarding.data.OnboardingRepository
import javax.inject.Inject

class OnboardingService @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    /** True once the welcome tour has been finished; the tour only shows before that. */
    fun isOnboardingCompleted(): Boolean = onboardingRepository.isOnboardingCompleted()

    fun completeOnboarding() = onboardingRepository.setOnboardingCompleted()
}
