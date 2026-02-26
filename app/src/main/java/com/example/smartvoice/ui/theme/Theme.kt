package com.example.smartvoice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = DarkButton,
    primaryVariant = DarkButton,
    secondary = LightBlue,
    background = RegalNavyDeep,
    surface = RegalNavyDeep,
    onPrimary = OnDarkButton,
    onSecondary = White,
    onBackground = White,
    onSurface = White,
    error = ErrorRed
)

private val LightColorPalette = lightColors(
    primary = BrightBlue,
    primaryVariant = LogoBlue,
    secondary = LightBlue,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = GreyDark,
    onSurface = GreyDark,
    error = ErrorRed
)

@Composable
fun SmartVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}