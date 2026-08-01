package com.iyas.budgetin.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = lightColorScheme(
    primary = NeoPink,
    onPrimary = Color.White,
    secondary = NeoYellow,
    onSecondary = SolidBlack,
    tertiary = NeoTeal,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondary,
    outline = SolidBlack,
    error = ExpenseRed,
    primaryContainer = NeoPink.copy(alpha = 0.1f),
    onPrimaryContainer = NeoPink,
)

@Composable
fun BudgetInTheme(
    darkTheme: Boolean = false, // Force light theme for this clean minimalist design
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundLight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}