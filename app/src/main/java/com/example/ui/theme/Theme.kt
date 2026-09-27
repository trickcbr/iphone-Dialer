package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IosGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A24),
    onPrimaryContainer = Color(0xFF86EFAC),
    secondary = IosBlue,
    onSecondary = Color.White,
    error = IosRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider
)

private val LightColorScheme = lightColorScheme(
    primary = IosGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FADF),
    onPrimaryContainer = Color(0xFF0F5132),
    secondary = IosBlue,
    onSecondary = Color.White,
    error = IosRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
