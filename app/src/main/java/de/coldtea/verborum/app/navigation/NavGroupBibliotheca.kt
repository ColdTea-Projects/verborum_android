package de.coldtea.verborum.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.bibliotheca.dictionary.ui.DictionaryListScreen

fun NavGraphBuilder.insertDictionariesList(navController: NavHostController) = composable(
    SCREEN_DICTIONARIES_LIST
) {
    DictionaryListScreen()
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