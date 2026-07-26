package de.coldtea.verborum.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.core.options.ui.OptionsScreen

fun NavGraphBuilder.insertOptions(navController: NavHostController) = composable(
    SCREEN_OPTIONS
) {
    // Tab root: the screen registers its own (localized) top bar with no back button.
    OptionsScreen()
}
