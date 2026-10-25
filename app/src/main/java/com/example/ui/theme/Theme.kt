package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonPurpleSecondary,
    tertiary = BrightYellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = OnDarkBackground,
    onSurface = OnDarkSurface,
    primaryContainer = Color(0x33CA72FF),
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    secondary = NeonPurpleSecondary,
    tertiary = BrightYellow,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = OnLightSurface,
    onTertiary = Color.Black,
    onBackground = OnLightBackground,
    onSurface = OnLightSurface,
    primaryContainer = Color(0xFFF1E6FF),
    onPrimaryContainer = Color(0xFF5E00A6)
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
