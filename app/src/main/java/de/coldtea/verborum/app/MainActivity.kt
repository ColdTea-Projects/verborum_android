package de.coldtea.verborum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.verborum.app.navigation.NavigationCentral
import de.coldtea.verborum.bibliotheca.onboarding.domain.OnboardingService
import de.coldtea.verborum.core.theme.VerborumTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingService: OnboardingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Decided once per launch: the welcome tour only shows until it is completed.
        val showWelcome = !onboardingService.isOnboardingCompleted()
        setContent {
            VerborumTheme {
                NavigationCentral(showWelcome = showWelcome)
            }
        }
    }
}
