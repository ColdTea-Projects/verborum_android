package de.coldtea.verborum.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** An optional icon button a screen can place on the right of the shared top bar. */
data class VerborumTopBarAction(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Content shown in the app's shared top bar. Screens don't draw their own header
 * anymore — they declare it with [RegisterTopBar] and the single Scaffold topBar
 * in the navigation host renders it.
 */
data class VerborumTopBarState(
    val title: String = "",
    val subtitle: String? = null,
    val showBackButton: Boolean = false,
    val action: VerborumTopBarAction? = null,
)

/** Holds the top bar content the currently visible screen has registered. */
class VerborumTopBarController {
    var state by mutableStateOf(VerborumTopBarState())
        private set

    fun update(
        title: String,
        subtitle: String?,
        showBackButton: Boolean,
        action: VerborumTopBarAction?,
    ) {
        state = VerborumTopBarState(title, subtitle, showBackButton, action)
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
    action: VerborumTopBarAction? = null,
) {
    val controller = LocalVerborumTopBarController.current
    // Keyed on the action's icon (not the object) since its onClick is a fresh lambda each
    // recomposition; that lambda closes over remembered state, so a "stale" one still works.
    LaunchedEffect(title, subtitle, showBackButton, action?.iconRes) {
        controller.update(title, subtitle, showBackButton, action)
    }
}
