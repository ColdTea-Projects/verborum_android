package de.coldtea.verborum.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.forum.marketplace.ui.ForumMainScreen

fun NavGraphBuilder.insertForumMain(navController: NavHostController) = composable(
    SCREEN_FORUM_MAIN_SCREEN
) {
    ForumMainScreen()
}