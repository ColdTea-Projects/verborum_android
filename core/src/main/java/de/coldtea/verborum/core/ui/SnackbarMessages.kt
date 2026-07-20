package de.coldtea.verborum.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow

/**
 * The app-wide [SnackbarHostState], provided once by the navigation scaffold so any screen can
 * surface a message without threading the host through its call site — mirrors
 * [LocalVerborumTopBarController]. Defaults to a throwaway instance so screen previews (which do
 * not provide one) keep working.
 */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

/**
 * Collects [messages] — typically a ViewModel's error/notification flow — and shows each on the
 * shared snackbar. Drop into any screen so its ViewModel's messages become visible; keeps error
 * reporting uniform across the app instead of each screen re-implementing the collection.
 */
@Composable
fun ShowSnackbarMessages(messages: Flow<UiText>) {
    val hostState = LocalSnackbarHostState.current
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { message ->
            hostState.showSnackbar(message = message.resolve(context), duration = SnackbarDuration.Short)
        }
    }
}
