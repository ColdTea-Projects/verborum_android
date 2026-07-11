package de.coldtea.verborum.bibliotheca.onboarding.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.onboarding.domain.OnboardingService
import de.coldtea.verborum.core.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val onboardingService: OnboardingService,
) : BaseViewModel() {

    fun completeOnboarding() = onboardingService.completeOnboarding()
}
