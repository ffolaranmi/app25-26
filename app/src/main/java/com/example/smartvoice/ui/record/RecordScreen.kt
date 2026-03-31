package com.example.smartvoice.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.R
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.child.ChildInfoDestination
import com.example.smartvoice.ui.child.ChildViewModel
import com.example.smartvoice.ui.child.ChildViewModelFactory
import com.example.smartvoice.ui.components.SmartVoiceBottomBar
import com.example.smartvoice.ui.components.SmartVoiceTopBar
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.results.ResultsDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartvoice.ui.tutorial.TutorialOverlay
import com.example.smartvoice.ui.tutorial.homeTutorialSteps
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

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

private val TileTextColor = Color(0xFF111827)

private fun dontShowPrefKey(userId: Long) = "dont_show_recording_instructions_$userId"

@Composable
fun RecordScreen(
    navigateToScreenOption: (NavigationDestination) -> Unit,
    navigateBack: () -> Unit,
    viewModelFactory: ViewModelProvider.Factory,
    database: SmartVoiceDatabase? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: RecordViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userId = remember { SessionPrefs.getLoggedInUserId(context) }

    val childVm: ChildViewModel? = if (database != null) {
        viewModel(factory = ChildViewModelFactory(database))
    } else null

    val children by childVm?.children?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var isRecording by remember { mutableStateOf(false) }
    var currentSecond by remember { mutableStateOf(0) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var selectedChildName by remember { mutableStateOf<String?>(null) }
    var selectedChildId by remember { mutableStateOf<Long?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessageText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var dontRemindAgain by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    val dontShowPermanently = remember(userId) {
        val prefs = context.getSharedPreferences("smartvoice_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getBoolean(dontShowPrefKey(userId), false)
    }

    val areButtonsEnabled = !isProcessing

    LaunchedEffect(userId) {
        if (userId != -1L) childVm?.loadChildren(userId)
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("smartvoice_prefs", android.content.Context.MODE_PRIVATE)
        val dontShow = prefs.getBoolean(dontShowPrefKey(userId), false)
        if (!dontShow) showInstructionsDialog = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showNameDialog = true else showPermissionDialog = true
    }

    fun onMicTapped() {
        when {
            isRecording -> {}
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> showNameDialog = true
            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = {
                if (dontRemindAgain) {
                    val prefs = context.getSharedPreferences("smartvoice_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(dontShowPrefKey(userId), true).apply()
                }
                showInstructionsDialog = false
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = White,
            title = {
                Text(
                    "Recording Instructions",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = LogoBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "First, tap the microphone to select the child you are recording for and tap Start Recording. The app will automatically start recording a 5-second sample for you.\n\nAsk the patient to sustain a steady vowel /a/ sound ('aah') for the full 5 seconds in a quiet environment, holding the phone approximately \n 20 cm away at a 45° angle.",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF1F2937)
                    )

                    if (!dontShowPermanently) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = dontRemindAgain,
                                onCheckedChange = { dontRemindAgain = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BrightBlue,
                                    uncheckedColor = Color(0xFF9CA3AF)
                                )
                            )
                            Text(
                                "Don't remind me again",
                                fontFamily = InterFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.clickable { dontRemindAgain = !dontRemindAgain }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dontRemindAgain) {
                            val prefs = context.getSharedPreferences("smartvoice_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putBoolean(dontShowPrefKey(userId), true).apply()
                        }
                        showInstructionsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, color = White)
                }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Microphone Permission Required",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = LogoBlue
                )
            },
            text = {
                Text(
                    "SmartVoice needs microphone access. Please grant permission in Settings.",
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    color = TileTextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPermissionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, color = White)
                }
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isRecording && !isProcessing) {
                    showNameDialog = false
                    selectedChildName = null
                    selectedChildId = null
                    nameError = false
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = White,
            title = null,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (children.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(color = ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.PeopleOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Children Added", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LogoBlue)
                            Text("Add a child in Child Info to start recording.", fontFamily = InterFont, fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center, lineHeight = 18.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(color = BrightBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.PeopleOutline, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(32.dp))
                        }
                        Text("Select Child", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LogoBlue)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = PillGrey, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                children.forEach { child ->
                                    val childDisplayName = "${child.firstName} ${child.lastName}".trim()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = if (selectedChildId == child.id) BrightBlue.copy(alpha = 0.2f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = areButtonsEnabled) {
                                                selectedChildName = childDisplayName
                                                selectedChildId = child.id
                                                nameError = false
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Text(childDisplayName, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = if (areButtonsEnabled) TileTextColor else TileTextColor.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                        if (nameError) {
                            Text("Please select a child.", fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
                        }
                    }
                }
            },
            confirmButton = {
                if (children.isEmpty()) {
                    Button(
                        onClick = { showNameDialog = false; navigateToScreenOption(ChildInfoDestination) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = areButtonsEnabled
                    ) {
                        Text("Add Child", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = White)
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedChildName.isNullOrBlank()) {
                                nameError = true
                            } else {
                                val capturedName = selectedChildName!!.trim()
                                showNameDialog = false
                                nameError = false

                                coroutineScope.launch {
                                    try {
                                        isRecording = true
                                        isProcessing = true
                                        currentSecond = 0
                                        showSuccessMessage = false

                                        val recordingJob = async(Dispatchers.IO) {
                                            viewModel.startRecording(context, seconds = 5)
                                        }

                                        for (second in 1..5) {
                                            delay(1000)
                                            currentSecond = second
                                            Log.d("RecordScreen", "Current second: $second")
                                        }

                                        recordingJob.await()

                                        if (isRecording) {
                                            withContext(Dispatchers.IO) { viewModel.stopRecording() }
                                            isRecording = false

                                            val wavFile = withContext(Dispatchers.IO) { viewModel.getLastWavFile() }

                                            if (wavFile == null || !wavFile.exists()) {
                                                successMessageText = "RECORDING FAILED"
                                                showSuccessMessage = true
                                                delay(1500)
                                                showSuccessMessage = false
                                                isProcessing = false
                                                currentSecond = 0
                                                return@launch
                                            }

                                            successMessageText = "PLEASE WAIT WHILE YOUR \nRECORDING IS BEING PROCESSED"
                                            showSuccessMessage = true

                                            val mlScore = withContext(Dispatchers.IO) {
                                                try {
                                                    viewModel.runModel(wavFile)
                                                } catch (e: Exception) {
                                                    Log.e("RecordScreen", "Model inference failed", e)
                                                    "Pending"
                                                }
                                            }

                                            val recordingCount = withContext(Dispatchers.IO) {
                                                childVm?.getRecordingCountForChild(capturedName, userId = userId) ?: 0
                                            }

                                            val recordingLabel = "$capturedName recording ${recordingCount + 1}"
                                            val dateTimeStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

                                            val diagnosis = DiagnosisTable(
                                                userId = userId,
                                                patientName = recordingLabel,
                                                diagnosis = mlScore,
                                                recordingDate = dateTimeStr,
                                                recordingLength = "5s",
                                                recordingPath = wavFile.absolutePath
                                            )

                                            withContext(Dispatchers.IO) { viewModel.insertDiagnosis(diagnosis) }

                                            successMessageText = "RECORDING SAVED"
                                            showSuccessMessage = true
                                            delay(1500)
                                            showSuccessMessage = false
                                            isProcessing = false
                                            selectedChildName = null
                                            selectedChildId = null
                                            currentSecond = 0
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RecordScreen", "Error during recording", e)
                                        successMessageText = "ERROR"
                                        showSuccessMessage = true
                                        delay(1500)
                                        showSuccessMessage = false
                                        isRecording = false
                                        isProcessing = false
                                        currentSecond = 0
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue, disabledContainerColor = BrightBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = areButtonsEnabled
                    ) {
                        Text(if (isRecording) "Recording..." else "Start Recording", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNameDialog = false; selectedChildName = null; selectedChildId = null; nameError = false },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    enabled = areButtonsEnabled
                ) {
                    Text("Cancel", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (areButtonsEnabled) Color(0xFF9CA3AF) else Color(0xFF9CA3AF).copy(alpha = 0.5f))
                }
            }
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                SmartVoiceTopBar(
                    title = "Recording",
                    onBack = { navigateBack() },
                    enabled = areButtonsEnabled,
                    onHelp = { showTutorial = true }
                )

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .clip(CircleShape)
                            .clickable(enabled = areButtonsEnabled && !isRecording) { onMicTapped() },
                        contentAlignment = Alignment.Center
                    ) {
                        RecordingMicDisplay(isRecording = isRecording)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        for (i in 1..5) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (i <= currentSecond) BrightBlue else BrightBlue.copy(alpha = 0.15f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    )

                    {
                        ActionSideButton(
                            text = "i",
                            onClick = { showInstructionsDialog = true },
                            enabled = areButtonsEnabled,
                            modifier = Modifier.size(width = 120.dp, height = 120.dp)
                        )
                        Spacer(modifier = Modifier.width(34.dp))
                        ActionSideIconButton(
                            onClick = { navigateToScreenOption(ResultsDestination) },
                            enabled = areButtonsEnabled,
                            modifier = Modifier.size(width = 120.dp, height = 120.dp),
                            painterRes = R.drawable.results,
                            contentDescription = "Results"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .height(90.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showSuccessMessage,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            if (successMessageText == "PLEASE WAIT WHILE YOUR \nRECORDING IS BEING PROCESSED") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = successMessageText,
                                        fontFamily = InterFont,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LogoBlue,
                                        textAlign = TextAlign.Center
                                    )

                                    SoundWaveLoader(
                                        color = LogoBlue,
                                        bars = 9,
                                        modifier = Modifier
                                            .height(16.dp)
                                            .wrapContentWidth()
                                    )
                                }
                            } else {
                                Text(
                                    text = successMessageText,
                                    fontFamily = InterFont,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LogoBlue,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                SmartVoiceBottomBar(onHomeClick = navigateBack, enabled = areButtonsEnabled)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showTutorial) {
        TutorialOverlay(
            steps = homeTutorialSteps,
            onFinish = { showTutorial = false }
        )
    }

}

@Composable
private fun ActionSideButton(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) BrightBlue.copy(alpha = 0.14f) else BrightBlue.copy(alpha = 0.07f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, color = if (enabled) BrightBlue else BrightBlue.copy(alpha = 0.5f))
    }
}
@Composable
private fun SoundWaveLoader(
    modifier: Modifier = Modifier,
    color: Color,
    bars: Int = 9
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
        modifier = modifier.alpha(0.95f),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { h ->
            val heightDp = (14.dp * h.value).coerceAtLeast(4.dp)

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightDp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}



@Composable
private fun ActionSideIconButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier, painterRes: Int, contentDescription: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) BrightBlue.copy(alpha = 0.14f) else BrightBlue.copy(alpha = 0.07f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = painterRes), contentDescription = contentDescription, tint = if (enabled) BrightBlue else BrightBlue.copy(alpha = 0.5f), modifier = Modifier.fillMaxSize(0.85f))
    }
}

@Composable
private fun RecordingMicDisplay(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_mic_transition")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isRecording) 1.14f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (isRecording) 900 else 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "outer_scale"
    )
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f, targetValue = if (isRecording) 0.34f else 0.18f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (isRecording) 900 else 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "outer_alpha"
    )
    val innerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isRecording) 1.06f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (isRecording) 700 else 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "inner_scale"
    )

    Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(280.dp).scale(outerScale).alpha(outerAlpha).clip(CircleShape).background(BrightBlue))
        Box(
            modifier = Modifier.size(220.dp).scale(innerScale).clip(CircleShape).background(BrightBlue.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(id = R.drawable.mic), contentDescription = "Microphone", tint = BrightBlue, modifier = Modifier.size(260.dp).offset(x = 10.dp))
        }
    }
}