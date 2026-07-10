package de.coldtea.verborum.app.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.coldtea.verborum.bibliotheca.dictionary.ui.DictionaryListScreen
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.CreateDictionaryScreen
import de.coldtea.verborum.bibliotheca.word.ui.createword.CreateWordScreen
import de.coldtea.verborum.bibliotheca.word.ui.createword.CreateWordViewModel
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.DictionaryDetailsScreen
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.DictionaryDetailsViewModel
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.MultipleChoiceQuestionScreen
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.MultipleChoiceViewModel
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.SelfPracticeScreen
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.SelfPracticeViewModel

fun NavGraphBuilder.insertDictionariesList(navController: NavHostController) = composable(
    SCREEN_DICTIONARIES_LIST
) {
    DictionaryListScreen(
        onDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_DICTIONARIES_DETAIL/$dictionaryId")
        },
        onCreateDictionaryClick = {
            navController.navigate(SCREEN_CREATE_DICTIONARY)
        }
    )
}

fun NavGraphBuilder.insertCreateDictionary(navController: NavHostController) = composable(
    SCREEN_CREATE_DICTIONARY
) {
    CreateDictionaryScreen(
        onDictionaryCreated = { dictionaryId ->
            navController.navigate("$SCREEN_DICTIONARIES_DETAIL/$dictionaryId") {
                popUpTo(SCREEN_DICTIONARIES_LIST)
            }
        }
    )
}

fun NavGraphBuilder.insertCreateWord(navController: NavHostController) = composable(
    "$SCREEN_CREATE_WORD/{dictionaryId}"
) { navBackStackEntry ->
    val viewModel = hiltViewModel<CreateWordViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    viewModel.init(dictionaryId)

    CreateWordScreen(viewModel)
}

fun NavGraphBuilder.insertDictionariesDetails(navController: NavHostController) = composable(
    "$SCREEN_DICTIONARIES_DETAIL/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<DictionaryDetailsViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    viewModel.init(dictionaryId)

    DictionaryDetailsScreen(
        viewModel = viewModel,
        onTestClicked = {
            navController.navigate("$SCREEN_MULTIPLE_CHOCIE/$dictionaryId")
        },
        onSelfPracticeClicked = {
            navController.navigate("$SCREEN_SELF_PRACTICE/$dictionaryId")
        },
        onCreateWordClicked = {
            navController.navigate("$SCREEN_CREATE_WORD/$dictionaryId")
        }
    )
}

fun NavGraphBuilder.insertSelfPractice(navController: NavHostController) = composable(
    "$SCREEN_SELF_PRACTICE/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<SelfPracticeViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    viewModel.init(dictionaryId)

    SelfPracticeScreen(viewModel)
}

fun NavGraphBuilder.insertMultipleChoiceScreen(navController: NavHostController, snackbarHostState: SnackbarHostState) = composable(
    "$SCREEN_MULTIPLE_CHOCIE/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<MultipleChoiceViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    viewModel.init(dictionaryId)

    MultipleChoiceQuestionScreen(viewModel, snackbarHostState){
        navController.popBackStack()
    }
}