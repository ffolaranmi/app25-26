package com.example.smartvoice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SmartVoiceOutlinedTextFieldColors() =
    if (isSystemInDarkTheme()) {
        TextFieldDefaults.outlinedTextFieldColors(
            textColor = White,
            cursorColor = White,

            focusedBorderColor = White,
            unfocusedBorderColor = White.copy(alpha = 0.85f),

            focusedLabelColor = White,
            unfocusedLabelColor = White.copy(alpha = 0.85f),

            trailingIconColor = White,
            leadingIconColor = White,

            backgroundColor = DarkField
        )
    } else {
        TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colors.onSurface,
            cursorColor = MaterialTheme.colors.primary,

            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = Divider,

            focusedLabelColor = MaterialTheme.colors.primary,
            unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),

            trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
            leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),

            backgroundColor = Color.Transparent
        )
    }
