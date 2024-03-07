package de.coldtea.verborum.app.common.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import de.coldtea.verborum.app.dictionarieslist.ui.DictionariesListScreen
import de.coldtea.verborum.app.forummain.ui.ForumMainScreen

@Composable
fun NavigationCentral() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screenGroups.forEach { group ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = group.iconResourceId),
                                contentDescription = null
                            )
                        },
                        label = { Text(text = stringResource(id = group.textResourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == group.route } == true,
                        onClick = {
                            navController.navigate(group.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        })

                }
            }
        }
    ) { innerPadding ->
        val context = LocalContext.current
        // Apply the padding globally to the whole BottomNavScreensController
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = GROUP_BIBLIOTHECA) {
                navigation(startDestination = SCREEN_DICTIONARIES_LIST, route = GROUP_BIBLIOTHECA) {
                    composable(SCREEN_DICTIONARIES_LIST) {
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
//                    composable("$SCREEN_CONTENT_DETAIL/{anime_id}") { navBackStackEntry ->
//
//                    }

                }
                navigation(startDestination = SCREEN_FORUM_MAIN_SCREEN, route = GROUP_FORUM) {
                    composable(SCREEN_FORUM_MAIN_SCREEN) {
                        ForumMainScreen()
                    }
                }
            }
        }
    }
}