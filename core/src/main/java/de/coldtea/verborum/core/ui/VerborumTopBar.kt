package de.coldtea.verborum.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Content shown in the app's shared top bar. Screens don't draw their own header
 * anymore — they declare it with [RegisterTopBar] and the single Scaffold topBar
 * in the navigation host renders it.
 */
data class VerborumTopBarState(
    val title: String = "",
    val subtitle: String? = null,
    val showBackButton: Boolean = false,
)

/** Holds the top bar content the currently visible screen has registered. */
class VerborumTopBarController {
    var state by mutableStateOf(VerborumTopBarState())
        private set

    fun update(title: String, subtitle: String?, showBackButton: Boolean) {
        state = VerborumTopBarState(title, subtitle, showBackButton)
    }
}

/**
 * Defaults to a throwaway controller so previews of individual screens (which
 * don't provide one) keep working; the real controller is provided in NavigationCentral.
 */
val LocalVerborumTopBarController = staticCompositionLocalOf { VerborumTopBarController() }

/**
 * Declares the top bar for the enclosing screen. Call it from the screen composable
 * (guarded by whatever state supplies a dynamic title). Roots pass
 * [showBackButton] = false; screens navigated into pass true.
 */
@Composable
fun RegisterTopBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = true,
) {
    val controller = LocalVerborumTopBarController.current
    LaunchedEffect(title, subtitle, showBackButton) {
        controller.update(title, subtitle, showBackButton)
    }
}
