package de.coldtea.verborum.app.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.core.R as CoreR
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.forum.marketplace.ui.ForumMainScreen

fun NavGraphBuilder.insertForumMain(navController: NavHostController) = composable(
    de.coldtea.verborum.app.navigation.SCREEN_FORUM_MAIN_SCREEN
) {
    // Forum is a tab root — title/subtitle only, no back button. The title is the product name and
    // stays literal; the subtitle is prose and is translated.
    RegisterTopBar(
        title = "Forum",
        subtitle = stringResource(CoreR.string.forumComingSoon),
        showBackButton = false,
    )
    ForumMainScreen()
}