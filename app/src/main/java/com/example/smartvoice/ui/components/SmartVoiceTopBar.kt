package com.example.smartvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartvoice.R
import com.example.smartvoice.ui.theme.LogoBlue

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

@Composable
fun SmartVoiceTopBar(
    title: String,
    onBack: () -> Unit,
    enabled: Boolean = true,
    fontSize: Int = 40
) {

    val adjustedFontSize = when {
        title.length > 24 -> (fontSize - 14).sp
        title.length > 16 -> (fontSize - 8).sp
        else -> fontSize.sp
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onBack,
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (enabled) LogoBlue else LogoBlue.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = title,
            fontFamily = InterFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = adjustedFontSize,
            letterSpacing = (-2.5).sp,
            color = LogoBlue,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = (adjustedFontSize.value * 1.1f).sp,
            modifier = Modifier.weight(1f)
        )
    }
}