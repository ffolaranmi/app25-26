package com.example.smartvoice.ui.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.R
import com.example.smartvoice.ui.History.HistoryDestination
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object RecordDestination : NavigationDestination {
    override val route = "record"
    override val titleRes = R.string.app_name
}

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private val MicGrey = Color(0xFF444444)
private val RecordRed = Color(0xFFD94F4F)

@Composable
fun RecordScreen(
    navigateToScreenOption: (NavigationDestination) -> Unit,
    navigateBack: () -> Unit,
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier
) {
    val viewModel: RecordViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var recordingSavedMessage by remember { mutableStateOf("") }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Recording Instructions",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = LogoBlue
                )
            },
            text = {
                Text(
                    text = "Please ask the patient to sustain a steady vowel sound - either \"aah\" or \"eee\" - continuously for approximately 5 seconds in a quiet environment.\n\nEnsure the device is held at a consistent distance from the patient's mouth throughout the recording.",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF1F2937)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "OK",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { navigateBack() }) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = LogoBlue,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Text(
                            text = "Recording",
                            fontFamily = com.example.smartvoice.ui.record.InterFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            letterSpacing = (-2.5).sp,
                            color = LogoBlue,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Icon(
                        imageVector = Icons.Outlined.MicNone,
                        contentDescription = "Microphone",
                        tint = if (isRecording) RecordRed else MicGrey,
                        modifier = Modifier.size(160.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    SoundWave(
                        isActive = isRecording,
                        color = if (isRecording) RecordRed.copy(alpha = 0.8f) else MicGrey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(LogoBlue)
                                .clickable { showInfoDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "i",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = White,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BrightBlue)
                                .clickable { navigateToScreenOption(HistoryDestination) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = "History",
                                tint = White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(RecordRed)
                            .clickable {
                                if (!isRecording) {
                                    isRecording = true
                                    recordingSavedMessage = ""
                                    coroutineScope.launch {
                                        viewModel.startRecording(context)
                                        viewModel.stopRecording()
                                        isRecording = false
                                        recordingSavedMessage = "Recording saved"
                                        delay(3000)
                                        recordingSavedMessage = ""
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isRecording) 32.dp else 44.dp)
                                .clip(if (isRecording) RoundedCornerShape(7.dp) else CircleShape)
                                .background(White.copy(alpha = 0.9f))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = recordingSavedMessage.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Text(
                            text = recordingSavedMessage,
                            fontFamily = InterFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrightBlue
                        )
                    }
                }

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun SoundWave(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    bars: Int = 32
) {
    val transition = rememberInfiniteTransition(label = "wave")

    val heights = (0 until bars).map { i ->
        if (isActive) {
            val delay = (i * 40) % 600
            transition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 480,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )
        } else {
            val staticVal = when {
                i < 2 || i >= bars - 2   -> 0.1f
                i < 5 || i >= bars - 5   -> 0.25f
                i < 8 || i >= bars - 8   -> 0.45f
                i < 11 || i >= bars - 11 -> 0.65f
                i < 14 || i >= bars - 14 -> 0.82f
                else                      -> 0.95f
            }
            remember { mutableStateOf(staticVal) }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { h ->
            val fraction = if (h is State<*>) (h.value as? Float) ?: 0.5f else 0.5f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}