package com.iyas.budgetin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = GreenTertiary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = ExpenseRed,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenTertiary,
)

private val LightColorScheme = lightColorScheme(
    primary = GreenSecondary,
    onPrimary = Color.White,
    secondary = GreenPrimary,
    onSecondary = Color.White,
    tertiary = GreenTertiary,
    background = BackgroundLight,
    onBackground = Color(0xFF1A2633),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A2633),
    surfaceVariant = CardLight,
    onSurfaceVariant = Color(0xFF4A6070),
    outline = Color(0xFFD0DDE8),
    error = ExpenseRed,
    primaryContainer = Color(0xFFD1FAF0),
    onPrimaryContainer = GreenDark,
)

@Composable
fun BudgetInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) BackgroundDark.toArgb() else BackgroundLight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}