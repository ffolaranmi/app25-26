package com.example.smartvoice.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartvoice.R

val SmartVoiceFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

val Typography = Typography(

    h3 = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp
    ),

    h4 = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),

    body1 = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),

    body2 = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),

    button = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),

    subtitle1 = TextStyle(
        fontFamily = SmartVoiceFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    )
)
