package de.coldtea.verborum.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.DictionaryListScreen
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.CreateDictionaryScreen
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.CreateDictionaryViewModel
import de.coldtea.verborum.bibliotheca.onboarding.ui.WelcomeScreen
import de.coldtea.verborum.bibliotheca.word.ui.createword.CreateWordScreen
import de.coldtea.verborum.bibliotheca.word.ui.createword.CreateWordViewModel
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.DictionaryDetailsScreen
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.DictionaryDetailsViewModel
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.MultipleChoiceQuestionScreen
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.MultipleChoiceViewModel
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.SelfPracticeScreen
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.SelfPracticeViewModel

fun NavGraphBuilder.insertWelcome(navController: NavHostController) = composable(
    SCREEN_WELCOME
) {
    WelcomeScreen(
        onDone = {
            navController.navigate(GROUP_BIBLIOTHECA) {
                popUpTo(SCREEN_WELCOME) { inclusive = true }
            }
        }
    )
}

fun NavGraphBuilder.insertDictionariesList(
    navController: NavHostController,
) = composable(
    SCREEN_DICTIONARIES_LIST
) {
    DictionaryListScreen(
        onDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_DICTIONARIES_DETAIL/$dictionaryId")
        },
        onCreateDictionaryClick = {
            navController.navigate(SCREEN_CREATE_DICTIONARY)
        },
        onEditDictionaryClick = { dictionaryId ->
            navController.navigate("$SCREEN_CREATE_DICTIONARY?dictionaryId=$dictionaryId")
        }
    )
}

fun NavGraphBuilder.insertCreateDictionary(navController: NavHostController) = composable(
    route = "$SCREEN_CREATE_DICTIONARY?dictionaryId={dictionaryId}",
    arguments = listOf(
        navArgument("dictionaryId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { navBackStackEntry ->
    val viewModel = hiltViewModel<CreateDictionaryViewModel>()
    val dictionaryId: String? = navBackStackEntry.arguments?.getString("dictionaryId")

    // Keyed on the destination's arguments so the screen is initialised once per navigation, not
    // on every recomposition of this destination.
    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

    CreateDictionaryScreen(
        viewModel = viewModel,
        onDictionaryCreated = { newId ->
            navController.navigate("$SCREEN_DICTIONARIES_DETAIL/$newId") {
                popUpTo(SCREEN_DICTIONARIES_LIST)
            }
        },
        onDictionaryUpdated = { navController.popBackStack() },
    )
}

fun NavGraphBuilder.insertCreateWord(navController: NavHostController) = composable(
    route = "$SCREEN_CREATE_WORD/{dictionaryId}?wordId={wordId}",
    arguments = listOf(
        navArgument("wordId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { navBackStackEntry ->
    val viewModel = hiltViewModel<CreateWordViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()
    val wordId: String? = navBackStackEntry.arguments?.getString("wordId")

    LaunchedEffect(dictionaryId, wordId) { viewModel.init(dictionaryId, wordId) }

    CreateWordScreen(
        viewModel = viewModel,
        onWordUpdated = { navController.popBackStack() },
    )
}

fun NavGraphBuilder.insertDictionariesDetails(navController: NavHostController) = composable(
    "$SCREEN_DICTIONARIES_DETAIL/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<DictionaryDetailsViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

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
        },
        onEditWordClicked = { wordId ->
            navController.navigate("$SCREEN_CREATE_WORD/$dictionaryId?wordId=$wordId")
        },
        onDictionaryDeleted = {
            navController.popBackStack()
        }
    )
}

fun NavGraphBuilder.insertSelfPractice(navController: NavHostController) = composable(
    "$SCREEN_SELF_PRACTICE/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<SelfPracticeViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

    SelfPracticeScreen(viewModel)
}

fun NavGraphBuilder.insertMultipleChoiceScreen(navController: NavHostController) = composable(
    "$SCREEN_MULTIPLE_CHOCIE/{dictionaryId}"
){ navBackStackEntry ->
    val viewModel = hiltViewModel<MultipleChoiceViewModel>()
    val dictionaryId: String = navBackStackEntry.arguments?.getString("dictionaryId").orEmpty()

    LaunchedEffect(dictionaryId) { viewModel.init(dictionaryId) }

    MultipleChoiceQuestionScreen(viewModel){
        navController.popBackStack()
    }
}