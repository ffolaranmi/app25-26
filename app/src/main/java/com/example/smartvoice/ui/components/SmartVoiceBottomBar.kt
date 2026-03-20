package com.example.smartvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
fun SmartVoiceBottomBar(
    onHomeClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        IconButton(
            onClick = onHomeClick,
            enabled = enabled,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = if (enabled) LogoBlue else LogoBlue.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "SmartVoice",
            fontFamily = InterFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            letterSpacing = (-1.5).sp,
            color = if (enabled) LogoBlue else LogoBlue.copy(alpha = 0.3f)
        )
    }
}