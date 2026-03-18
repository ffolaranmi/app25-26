package com.example.smartvoice.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smartvoice.R
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    GradientBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.smartvoicelogo),
                contentDescription = null,
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            SoundWaveLoader(
                color = if (isDark) BrightBlue else LogoBlue,
                bars = 13,
                modifier = Modifier
                    .height(22.dp)
                    .wrapContentWidth()
                    .alpha(0.95f)
            )
        }
    }
}

@Composable
private fun SoundWaveLoader(
    modifier: Modifier = Modifier,
    color: Color,
    bars: Int = 13
) {
    val transition = rememberInfiniteTransition(label = "wave")

    val heights = (0 until bars).map { i ->
        val delay = i * 60
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 550,
                    delayMillis = delay,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$i"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { h ->
            val heightDp = (18.dp * h.value).coerceAtLeast(5.dp)

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(heightDp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}