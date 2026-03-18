package com.example.smartvoice.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(RegalNavyDeep, RegalNavy, DarkField)
    } else {
        listOf(Color.White, GradientMid, LightBlue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) { content() }
}

@Composable
fun PaleBlueBackground(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) RegalNavyDeep else Color(0xFFDBE6F8)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) { content() }
}