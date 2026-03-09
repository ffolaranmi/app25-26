package com.example.smartvoice.ui.results

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ResultsDestination : NavigationDestination {
    override val route = "results"
    override val titleRes = R.string.results
}

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private val TileTextColor    = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)
private const val PROCESSING_DURATION_MS = 10_000L
private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

public sealed class DiagnosisResult {
    object Processing : DiagnosisResult()
    data class Ready(val pathology: Boolean, val probability: Double) : DiagnosisResult()
    object Error : DiagnosisResult()
}

private fun parseResult(diagnosis: String, recordingDate: String, nowMillis: Long): DiagnosisResult {
    val cleanDiagnosis = diagnosis.trim()

    if (cleanDiagnosis == "processing") {
        return DiagnosisResult.Processing
    }

    if (isStillProcessing(recordingDate, nowMillis)) return DiagnosisResult.Processing

    if (cleanDiagnosis.isEmpty() || cleanDiagnosis == "error" || cleanDiagnosis == "error|0.00") {
        return DiagnosisResult.Error
    }

    return try {
        val parts = cleanDiagnosis.split("|")

        if (parts.size >= 2) {
            val pathology = parts[0].trim().toBooleanStrictOrNull() ?: return DiagnosisResult.Error
            val prob = parts[1].trim().toDoubleOrNull() ?: return DiagnosisResult.Error
            DiagnosisResult.Ready(pathology, prob)
        } else {
            val singleValue = cleanDiagnosis.toIntOrNull()
            if (singleValue != null) {
                val probability = singleValue / 100.0
                val isPathology = singleValue >= 50
                DiagnosisResult.Ready(isPathology, probability)
            } else {
                DiagnosisResult.Error
            }
        }
    } catch (e: Exception) {
        DiagnosisResult.Error
    }
}

private fun isStillProcessing(recordingDate: String, nowMillis: Long): Boolean {
    return try {
        val recorded = dateTimeFormat.parse(recordingDate)?.time ?: return false
        val elapsed = nowMillis - recorded
        elapsed < PROCESSING_DURATION_MS
    } catch (e: Exception) {
        false
    }
}

private fun splitDateTime(recordingDate: String): Pair<String, String> {
    return try {
        val parts = recordingDate.split(" ")
        val date = parts.getOrNull(0) ?: recordingDate
        val time = parts.getOrNull(1)?.substring(0, 5) ?: ""
        Pair(date, time)
    } catch (e: Exception) {
        Pair(recordingDate, "")
    }
}

@Composable
fun ResultsScreen(
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
    navigateBack: () -> Unit,
    navigateToRecord: () -> Unit,
) {
    val viewModel: ResultsViewModel = viewModel(factory = viewModelFactory)
    val diagnoses by viewModel.diagnoses.collectAsState(initial = emptyList())
    val isServerConnected by viewModel.isServerConnected.collectAsState(initial = false)

    val sortedDiagnoses = remember(diagnoses) {
        diagnoses.sortedByDescending { d ->
            try {
                dateTimeFormat.parse(d.recordingDate)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    var deleteTargetIndex by remember { mutableStateOf<Int?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf<DiagnosisResult.Ready?>(null) }
    var selectedDiagnosisId by remember { mutableStateOf<Long?>(null) }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(250)
            now = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadDiagnoses() }

    LaunchedEffect(isServerConnected) {
        if (isServerConnected) {
            val failedDiagnoses = sortedDiagnoses.filter { diagnosis ->
                parseResult(diagnosis.diagnosis, diagnosis.recordingDate, now) is DiagnosisResult.Error
            }
            failedDiagnoses.forEach { diagnosis ->
                viewModel.retryFailedAnalysis(diagnosis)
            }
        }
    }

    deleteTargetIndex?.let { index ->
        val target = sortedDiagnoses.getOrNull(index)
        AlertDialog(
            onDismissRequest = { deleteTargetIndex = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Delete Recording?", fontFamily = InterFont, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Are you sure you want to delete the recording for " +
                                "${target?.patientName ?: "this patient"} " +
                                "on ${target?.recordingDate ?: "this date"}?",
                        fontFamily = InterFont
                    )
                    Text(
                        "This action cannot be undone.",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        target?.let { viewModel.deleteDiagnosis(it) }
                        deleteTargetIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontFamily = InterFont, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetIndex = null }) {
                    Text("Cancel", fontFamily = InterFont, color = LogoBlue)
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Clear All Recordings?", fontFamily = InterFont, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you sure you want to delete ALL voice recordings?", fontFamily = InterFont)
                    Text(
                        "Recently deleted recordings cannot be recovered.",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllDiagnoses(); showClearAllDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All", fontFamily = InterFont, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", fontFamily = InterFont, color = LogoBlue)
                }
            }
        )
    }

    showResultDialog?.let { result ->
        val pct = (result.probability * 100).toInt()
        val isHighRisk = result.pathology
        val badgeColor = if (isHighRisk) ErrorRed else Color(0xFF2E7D32)
        val riskLabel = if (isHighRisk) "High Risk" else "Low Risk"
        val description =
            if (isHighRisk)
                "The voice sample shows signs consistent with pathology. We recommend consulting a medical professional for further assessment."
            else
                "The voice sample does not show significant signs of pathology. Continue monitoring as advised."

        AlertDialog(
            onDismissRequest = {
                selectedDiagnosisId?.let { viewModel.markAsViewed(it) }
                showResultDialog = null
                selectedDiagnosisId = null
            },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Analysis Result",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    color = LogoBlue
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = badgeColor.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$pct%",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 40.sp,
                                color = badgeColor
                            )
                            Text(
                                text = riskLabel,
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = badgeColor
                            )
                        }
                    }
                    Text(
                        text = description,
                        fontFamily = InterFont,
                        fontSize = 14.sp,
                        color = TileTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "This is not a medical diagnosis. Always consult a qualified clinician.",
                        fontFamily = InterFont,
                        fontSize = 11.sp,
                        color = PlaceholderColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDiagnosisId?.let { viewModel.markAsViewed(it) }
                        showResultDialog = null
                        selectedDiagnosisId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White)
                }
            }
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "Results",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sortedDiagnoses.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No voice samples currently available.",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = PlaceholderColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 0.dp)
                        ) {
                            itemsIndexed(sortedDiagnoses) { index, diagnosis ->
                                val result = parseResult(diagnosis.diagnosis, diagnosis.recordingDate, now)
                                val (date, time) = splitDateTime(diagnosis.recordingDate)
                                ResultsRecordingTile(
                                    patientName = diagnosis.patientName.replace("recording", "Recording"),
                                    recordingDate = date,
                                    recordingTime = time,
                                    result = result,
                                    isViewed = diagnosis.isViewed,
                                    recordingPath = diagnosis.recordingPath,
                                    onDeleteClick = { deleteTargetIndex = index },
                                    onResultsClick = {
                                        if (result is DiagnosisResult.Ready) {
                                            selectedDiagnosisId = diagnosis.id
                                            showResultDialog = result
                                        }
                                    },
                                    onRetryClick = {
                                        viewModel.retryFailedAnalysis(diagnosis)
                                    }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFF7FBFD),
                                            Color(0xFFF7FBFD).copy(alpha = 0f)
                                        ),
                                        startY = 0f,
                                        endY = 45f
                                    )
                                )
                                .pointerInput(Unit) {}
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFC3D6F5).copy(alpha = 0f),
                                            Color(0xFFC3D6F5 )
                                        ),
                                        startY = 0f,
                                        endY = 60f
                                    )
                                )
                                .pointerInput(Unit) {}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navigateToRecord() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MicNone,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Recording",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Clear All",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.weight(0.05f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-1.5).sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ResultsRecordingTile(
    patientName: String,
    recordingDate: String,
    recordingTime: String,
    result: DiagnosisResult,
    isViewed: Boolean,
    recordingPath: String,
    onDeleteClick: () -> Unit,
    onResultsClick: () -> Unit,
    onRetryClick: () -> Unit,
    context: android.content.Context = LocalContext.current
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playbackProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && recordingPath.isNotEmpty()) {
            try {
                val player = MediaPlayer().apply {
                    setDataSource(recordingPath)
                    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
                    audioManager?.requestAudioFocus(null, android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.AUDIOFOCUS_GAIN)
                    setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                    prepare()
                    setVolume(1.0f, 1.0f)
                    start()
                }
                mediaPlayer = player

                while (isPlaying && player.isPlaying) {
                    playbackProgress = player.currentPosition.toFloat() / player.duration.toFloat()
                    kotlinx.coroutines.delay(50)
                }

                isPlaying = false
                player.release()
                mediaPlayer = null
                playbackProgress = 0f
            } catch (e: Exception) {
                isPlaying = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    val backgroundColor = if (isViewed) PillGrey else Color(0xFFE3F2FD)
    val titleColor = if (isViewed) TileTextColor else LogoBlue

    val fileExists = remember(recordingPath) {
        val file = File(recordingPath)
        file.exists() && file.length() > 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = patientName,
                            fontFamily = InterFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = titleColor
                        )
                        if (!isViewed) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = LogoBlue,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recordingDate,
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = PlaceholderColor
                        )
                        if (recordingTime.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(
                                        color = PlaceholderColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                            Text(
                                text = recordingTime,
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = PlaceholderColor
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                if (recordingPath.isNotEmpty() && fileExists) {
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp),
                        enabled = fileExists
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (fileExists) BrightBlue else Color(0xFFCFD8DC),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFFE3EFF8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(playbackProgress)
                            .background(BrightBlue)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF0F4FF),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (result) {
                    is DiagnosisResult.Processing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(rotation)
                            ) {
                                drawArc(
                                    color = BrightBlue,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                            Text(
                                text = "Analysing...",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = BrightBlue
                            )
                        }
                        Button(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFFCFD8DC),
                                disabledContentColor = Color(0xFF90A4AE)
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Results",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    is DiagnosisResult.Ready -> {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF2E7D32).copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Ready",
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Button(
                            onClick = onResultsClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Results",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = White
                            )
                        }
                    }

                    is DiagnosisResult.Error -> {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFB71C1C).copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "⚠  Analysis failed",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                        Button(
                            onClick = {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                isPlaying = false
                                playbackProgress = 0f
                                onRetryClick()
                            },
                            enabled = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrightBlue,
                                contentColor = White
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Retry",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}