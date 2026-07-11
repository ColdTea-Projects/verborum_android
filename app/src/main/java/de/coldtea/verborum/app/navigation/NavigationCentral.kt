package de.coldtea.verborum.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import de.coldtea.verborum.app.R
import de.coldtea.verborum.core.ui.LocalVerborumTopBarController
import de.coldtea.verborum.core.ui.VerborumTopBarController
import de.coldtea.verborum.core.ui.VerborumTopBarState

@Composable
fun NavigationCentral(showWelcome: Boolean = false) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarController = remember { VerborumTopBarController() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isOnWelcomeScreen = currentRoute == SCREEN_WELCOME
    // Only the tab roots get the bottom navigation; deeper screens are exited via the back button.
    val isRootScreen =
        currentRoute == SCREEN_DICTIONARIES_LIST || currentRoute == SCREEN_FORUM_MAIN_SCREEN

    val topBarState = topBarController.state

    CompositionLocalProvider(LocalVerborumTopBarController provides topBarController) {
        Scaffold(
            topBar = {
                // Welcome is full screen; every other screen renders the shared header.
                if (!isOnWelcomeScreen && topBarState.title.isNotEmpty()) {
                    VerborumTopBar(
                        state = topBarState,
                        onBackClick = { navController.popBackStack() },
                    )
                }
            },
            bottomBar = { if (isRootScreen) VerborumNavigationBar(navController) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            // Apply the padding globally to the whole BottomNavScreensController
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    // Welcome is a top-level destination, not part of a tab group, so
                    // switching tabs can never re-show it once the tour is finished.
                    startDestination = if (showWelcome) SCREEN_WELCOME else GROUP_BIBLIOTHECA
                ) {
                    insertWelcome(navController)
                    navigation(
                        startDestination = SCREEN_DICTIONARIES_LIST,
                        route = GROUP_BIBLIOTHECA
                    ) {
                        insertDictionariesList(navController, snackbarHostState)
                        insertCreateDictionary(navController)
                        insertDictionariesDetails(navController)
                        insertCreateWord(navController)
                        insertSelfPractice(navController)
                        insertMultipleChoiceScreen(navController, snackbarHostState)
                    }
                    navigation(startDestination = SCREEN_FORUM_MAIN_SCREEN, route = GROUP_FORUM) {
                        insertForumMain(navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerborumTopBar(
    state: VerborumTopBarState,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            // Sit below the system status bar; the app draws edge-to-edge.
            .statusBarsPadding()
            .padding(
                start = if (state.showBackButton) 8.dp else 24.dp,
                end = 24.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            Text(
                text = state.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.5.sp
            )

            state.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun VerborumNavigationBar(navController: NavHostController) {
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
