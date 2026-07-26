package de.coldtea.verborum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.verborum.app.navigation.NavigationCentral
import de.coldtea.verborum.core.auth.ui.LoginScreen
import de.coldtea.verborum.bibliotheca.onboarding.domain.OnboardingService
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.theme.VerborumTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingService: OnboardingService

    @Inject
    lateinit var authTokenStore: AuthTokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Decided once per launch: the welcome tour only shows until it is completed.
        val showWelcome = !onboardingService.isOnboardingCompleted()
        setContent {
            VerborumTheme {
                // The login wall gates the whole app: a failed refresh clears the session and
                // flips this back to false, returning the user here (guide §5). null means the
                // encrypted store is still being read off-main — render nothing rather than flash
                // the login screen at an already-signed-in user.
                val loggedIn by authTokenStore.isLoggedIn.collectAsState()
                when (loggedIn) {
                    true -> NavigationCentral(showWelcome = showWelcome)
                    false -> LoginScreen()
                    null -> Unit
                }
            }
        }
    }
}
