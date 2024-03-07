package de.coldtea.verborum.app.common.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import de.coldtea.verborum.app.dictionarieslist.ui.DictionariesListScreen

fun NavGraphBuilder.insertDictionariesList(navController: NavHostController) = composable(SCREEN_DICTIONARIES_LIST) {
    DictionariesListScreen()
//                        val contentViewModel = hiltViewModel<ContentViewModel>()

//                        ContentScreen(
//                            viewModel = contentViewModel,
//                            onItemClicked = { id ->
//                                navController.navigate("$SCREEN_CONTENT_DETAIL/$id")
//                            },
//                            onSearchClicked = {
//                                navController.navigate(SCREEN_SEARCH)
//                            }
//                        )
}