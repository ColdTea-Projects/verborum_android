package de.coldtea.verborum.app.common.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import de.coldtea.verborum.app.dictionarieslist.ui.DictionariesListScreen
import de.coldtea.verborum.app.forummain.ui.ForumMainScreen

fun NavGraphBuilder.insertForumMain(navController: NavHostController) = composable(SCREEN_FORUM_MAIN_SCREEN) {
    ForumMainScreen()
}