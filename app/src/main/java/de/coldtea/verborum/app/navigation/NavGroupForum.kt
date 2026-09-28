package de.coldtea.verborum.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.ForumDictionaryDetailsScreen
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.ForumDictionaryDetailsViewModel
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.ForumDictionaryListScreen

// Forum is a tab root; the screen registers its own title/subtitle (no back button).
fun NavGraphBuilder.insertForumMain(navController: NavHostController) = composable(
    SCREEN_FORUM_MAIN_SCREEN
) {
    ForumDictionaryListScreen(
        onDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_FORUM_DICTIONARY_DETAILS/$dictionaryId")
        },
    )
}

fun NavGraphBuilder.insertForumDictionaryDetails() = composable(
    "$SCREEN_FORUM_DICTIONARY_DETAILS/{dictionaryId}"
) { navBackStackEntry ->
    val viewModel = hiltViewModel<ForumDictionaryDetailsViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

    ForumDictionaryDetailsScreen(viewModel = viewModel)
}
