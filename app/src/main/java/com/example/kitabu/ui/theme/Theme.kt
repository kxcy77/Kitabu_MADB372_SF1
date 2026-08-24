package com.example.kitabu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Paper,
    secondary = Navy,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = ForestGreenDark,
    secondary = NavyLight,
)

@Composable
fun KitabuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KitabuTypography,
        content = content,
    )
}
