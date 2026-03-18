package com.example.smartvoice.ui.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.smartvoice.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history
}

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private const val PROCESSING_DURATION_MS = 10_000L
private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

private fun isStillProcessing(recordingDate: String): Boolean {
    return try {
        val recorded = dateTimeFormat.parse(recordingDate)?.time ?: return false
        val elapsed  = System.currentTimeMillis() - recorded
        elapsed < PROCESSING_DURATION_MS
    } catch (e: Exception) {
        false
    }
}

private fun splitDateTime(recordingDate: String): Pair<String, String> {
    return try {
        val parts = recordingDate.split(" ")
        val date  = parts.getOrNull(0) ?: recordingDate
        val time  = parts.getOrNull(1)?.substring(0, 5) ?: ""
        Pair(date, time)
    } catch (e: Exception) {
        Pair(recordingDate, "")
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
    navigateBack: () -> Unit,
    navigateToRecord: () -> Unit,
) {
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
    val diagnoses by viewModel.diagnoses.collectAsState(initial = emptyList())
    val isDark = isSystemInDarkTheme()

    val sortedDiagnoses = remember(diagnoses) {
        diagnoses.sortedByDescending { d ->
            try { dateTimeFormat.parse(d.recordingDate)?.time ?: 0L }
            catch (e: Exception) { 0L }
        }
    }

    var deleteTargetIndex  by remember { mutableStateOf<Int?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showComingSoon     by remember { mutableStateOf(false) }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadDiagnoses() }

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
                    Text("Cancel", fontFamily = InterFont, color = if (isDark) LightBlue else LogoBlue)
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
                    Text("Cancel", fontFamily = InterFont, color = if (isDark) LightBlue else LogoBlue)
                }
            }
        )
    }

    if (showComingSoon) {
        AlertDialog(
            onDismissRequest = { showComingSoon = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Coming Soon", fontFamily = InterFont, fontWeight = FontWeight.Bold) },
            text  = { Text("This feature is not yet available.", fontFamily = InterFont) },
            confirmButton = {
                Button(
                    onClick = { showComingSoon = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) BrightBlue else LogoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK", fontFamily = InterFont, color = White)
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
                            tint = if (isDark) White else LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "History",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = if (isDark) White else LogoBlue,
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
                            color = if (isDark) DarkTextSecondary else Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {

                        itemsIndexed(sortedDiagnoses) { index, diagnosis ->
                            val isProcessing = isStillProcessing(diagnosis.recordingDate)
                            val (date, time) = splitDateTime(diagnosis.recordingDate)
                            HistoryRecordingTile(
                                patientName    = diagnosis.patientName,
                                recordingDate  = date,
                                recordingTime  = time,
                                isProcessing   = isProcessing,
                                onDeleteClick  = { deleteTargetIndex = index },
                                onResultsClick = { showComingSoon = true }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    onClick = { showComingSoon = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkPill else White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "View Past Recordings",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (isDark) White else LogoBlue
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
                    color = if (isDark) White else LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryRecordingTile(
    patientName: String,
    recordingDate: String,
    recordingTime: String,
    isProcessing: Boolean,
    onDeleteClick: () -> Unit,
    onResultsClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkPill else PillGrey)
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
                    Text(
                        text = patientName,
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (isDark) White else Color(0xFF111827)
                    )
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
                            color = if (isDark) DarkTextSecondary else Color(0xFF4B5563)
                        )
                        if (recordingTime.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(
                                        color = if (isDark) DarkTextSecondary.copy(alpha = 0.5f) else Color(0xFF4B5563).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                            Text(
                                text = recordingTime,
                                fontFamily = InterFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isDark) DarkTextSecondary else Color(0xFF4B5563)
                            )
                        }
                    }
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (isDark) DarkDivider else Color(0xFFD1D9E6))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isDark) DarkSurface else Color(0xFFF0F4FF),
                        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isProcessing) {
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
                                color      = BrightBlue,
                                startAngle = 0f,
                                sweepAngle = 270f,
                                useCenter  = false,
                                style      = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap   = StrokeCap.Round
                                )
                            )
                        }
                        Text(
                            text = "Processing results...",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = BrightBlue
                        )
                    }
                    Button(
                        onClick  = {},
                        enabled  = false,
                        shape    = RoundedCornerShape(20.dp),
                        colors   = ButtonDefaults.buttonColors(
                            disabledContainerColor = if (isDark) Color(0xFF334155) else Color(0xFFCFD8DC),
                            disabledContentColor   = if (isDark) Color(0xFF64748B) else Color(0xFF90A4AE)
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
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isDark) DarkReadyBg else Color(0xFF1B5E20).copy(alpha = 0.10f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✓  Ready",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDark) DarkReadyGreen else Color(0xFF2E7D32)
                        )
                    }
                    Button(
                        onClick = onResultsClick,
                        shape   = RoundedCornerShape(20.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = BrightBlue),
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
            }
        }
    }
}