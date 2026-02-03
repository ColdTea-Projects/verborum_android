package de.coldtea.verborum.app.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.ui.DictionaryListScreen
import de.coldtea.verborum.bibliotheca.word.ui.DictionaryDetailsScreen
import de.coldtea.verborum.bibliotheca.word.ui.DictionaryDetailsViewModel

fun NavGraphBuilder.insertDictionariesList(navController: NavHostController) = composable(
    SCREEN_DICTIONARIES_LIST
) {
    DictionaryListScreen(
        onDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_DICTIONARIES_DETAIL/$dictionaryId")
        }
    )
}

fun NavGraphBuilder.insertDictionariesDetails(navController: NavHostController) = composable(
    "$SCREEN_DICTIONARIES_DETAIL/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<DictionaryDetailsViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    viewModel.init(dictionaryId)

    DictionaryDetailsScreen(viewModel)
}