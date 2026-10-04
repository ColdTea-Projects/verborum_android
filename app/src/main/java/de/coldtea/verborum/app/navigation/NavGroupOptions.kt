package de.coldtea.verborum.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.core.options.ui.LanguageSelectionScreen
import de.coldtea.verborum.core.options.ui.OptionsScreen
import de.coldtea.verborum.core.profile.ui.profile.ProfileScreen

fun NavGraphBuilder.insertOptions(navController: NavHostController) {
    composable(SCREEN_OPTIONS) {
        // Tab root: the screen registers its own (localized) top bar with no back button.
        OptionsScreen(
            onProfileClick = { navController.navigate(SCREEN_PROFILE) },
            onLanguageClick = { navController.navigate(SCREEN_LANGUAGE) },
        )
    }

    composable(SCREEN_LANGUAGE) {
        // Deep screen: its top bar carries a back button (registered by the screen itself).
        LanguageSelectionScreen()
    }

    composable(SCREEN_PROFILE) {
        ProfileScreen()
    }
}
