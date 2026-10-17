package de.coldtea.verborum.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.ForumDictionaryDetailsScreen
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.ForumDictionaryDetailsViewModel
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.ForumDictionaryListScreen
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.ForumDictionaryListViewModel
import de.coldtea.verborum.forum.marketplace.ui.forumsearch.ForumSearchScreen
import de.coldtea.verborum.forum.marketplace.ui.forumsearch.ForumSearchViewModel

// Forum is a tab root; the screen registers its own title/subtitle (no back button).
fun NavGraphBuilder.insertForumMain(navController: NavHostController) = composable(
    SCREEN_FORUM_MAIN_SCREEN
) {
    ForumDictionaryListScreen(
        onDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_FORUM_DICTIONARY_DETAILS/$dictionaryId")
        },
        onSearchClick = { navController.navigate(SCREEN_FORUM_SEARCH) },
    )
}

/**
 * The Forum's search criteria. It edits a draft started from the list's current filter, and Search
 * applies the draft to the list's own ViewModel — the one scoped to the list's back-stack entry, so
 * the very instance the list screen observes — then pops back to it.
 */
fun NavGraphBuilder.insertForumSearch(navController: NavHostController) = composable(
    SCREEN_FORUM_SEARCH
) { navBackStackEntry ->
    val listEntry = remember(navBackStackEntry) { navController.getBackStackEntry(SCREEN_FORUM_MAIN_SCREEN) }
    val listViewModel = hiltViewModel<ForumDictionaryListViewModel>(listEntry)
    val viewModel = hiltViewModel<ForumSearchViewModel>()

    LaunchedEffect(Unit) { viewModel.init(listViewModel.filter.value) }

    ForumSearchScreen(
        viewModel = viewModel,
        onSearch = { filter ->
            listViewModel.applyFilter(filter)
            navController.popBackStack()
        },
    )
}

// The details are online-only: the screen is left as soon as the connection goes (or is) off.
fun NavGraphBuilder.insertForumDictionaryDetails(navController: NavHostController) = composable(
    "$SCREEN_FORUM_DICTIONARY_DETAILS/{dictionaryId}"
) { navBackStackEntry ->
    val viewModel = hiltViewModel<ForumDictionaryDetailsViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

    ForumDictionaryDetailsScreen(
        viewModel = viewModel,
        onConnectionLost = { navController.popBackStack() },
    )
}
