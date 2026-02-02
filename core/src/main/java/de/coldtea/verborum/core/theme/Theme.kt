package de.coldtea.verborum.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = VerborumColors.DarkBackground,
    surface = VerborumColors.DarkSurface,
    surfaceVariant = VerborumColors.DarkSurfaceAlt,
    onBackground = VerborumColors.DarkText,
    onSurface = VerborumColors.DarkText,
    primary = VerborumColors.DarkAccent,
    secondary = VerborumColors.DarkGold,
    onPrimary = Color.White,
    onSecondary = Color.White,
    outline = VerborumColors.DarkBorder,
    onSurfaceVariant = VerborumColors.DarkTextSecondary,
    tertiary = VerborumColors.DarkTextTertiary,
    // Required Material3 colors
    primaryContainer = VerborumColors.DarkSurface,
    onPrimaryContainer = VerborumColors.DarkText,
    secondaryContainer = VerborumColors.DarkSurface,
    onSecondaryContainer = VerborumColors.DarkText,
    tertiaryContainer = VerborumColors.DarkSurface,
    onTertiaryContainer = VerborumColors.DarkText,
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inversePrimary = VerborumColors.LightAccent,
    inverseSurface = VerborumColors.LightSurface,
    inverseOnSurface = VerborumColors.LightText,
    surfaceTint = VerborumColors.DarkAccent,
    outlineVariant = VerborumColors.DarkBorder,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    background = VerborumColors.LightBackground,
    surface = VerborumColors.LightSurface,
    surfaceVariant = VerborumColors.LightSurfaceAlt,
    onBackground = VerborumColors.LightText,
    onSurface = VerborumColors.LightText,
    primary = VerborumColors.LightAccent,
    secondary = VerborumColors.LightGold,
    onPrimary = Color.White,
    onSecondary = Color.White,
    outline = VerborumColors.LightBorder,
    onSurfaceVariant = VerborumColors.LightTextSecondary,
    tertiary = VerborumColors.LightTextTertiary,
    // Required Material3 colors
    primaryContainer = VerborumColors.LightSurface,
    onPrimaryContainer = VerborumColors.LightText,
    secondaryContainer = VerborumColors.LightSurface,
    onSecondaryContainer = VerborumColors.LightText,
    tertiaryContainer = VerborumColors.LightSurface,
    onTertiaryContainer = VerborumColors.LightText,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inversePrimary = VerborumColors.DarkAccent,
    inverseSurface = VerborumColors.DarkSurface,
    inverseOnSurface = VerborumColors.DarkText,
    surfaceTint = VerborumColors.LightAccent,
    outlineVariant = VerborumColors.LightBorder,
    scrim = Color.Black
)

@Composable
fun VerborumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}