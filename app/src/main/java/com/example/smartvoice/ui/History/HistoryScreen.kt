package com.example.smartvoice.ui.History

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.R
import com.example.smartvoice.SmartVoiceTopAppBar
import com.example.smartvoice.data.Classification
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.ui.navigation.NavigationDestination
import androidx.compose.foundation.clickable

object HistoryDestination : NavigationDestination {
    override val route = "History"
    override val titleRes = R.string.history
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
    navigateBack: () -> Unit,
) {
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
    val diagnoses by viewModel.diagnoses.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadDiagnoses()
    }

    Scaffold(
        topBar = {
            SmartVoiceTopAppBar(
                title = stringResource(id = HistoryDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.clearAllDiagnoses() },
                containerColor = Color(0xFFB71C1C),
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            ) {
                Text(
                    text = "Clear All",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        }
    ) { innerPadding ->
        if (diagnoses.isEmpty()) {
            Text(
                text = "No voice samples currently available.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(diagnoses.reversed()) { diagnosis ->

                    VoiceSampleBubble(
                        recordingDate = diagnosis.recordingDate,
                        patientName = diagnosis.patientName,
                        diagnosis = diagnosis.diagnosis
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceSampleBubble(
    recordingDate: String,
    patientName: String,
    diagnosis: String
) {
    val showDialog = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog.value = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "VOICE SAMPLE DATE: $recordingDate",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF512DA8)
            )
            Text(
                text = "PATIENT NAME: $patientName",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF512DA8),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Your child has a $diagnosis% chance of having RRP",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            },
            title = { Text("Diagnosis Details") },
            text = {
                Column {
                    Text(text = "Patient Name: $patientName", fontWeight = FontWeight.Bold)
                    Text(text = "Date: $recordingDate", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Your child has a $diagnosis% chance of having RRP")


                    val diagnosisValue = diagnosis.toIntOrNull()

                    if (diagnosisValue != null) {
                        if (diagnosisValue >= 50) {
                            Text("This means your child has a HIGH risk of having RRP.")
                            Text("It is recommended you contact the child's gp or contact 111 for further medical advice.")
                        } else {
                            Text("This means your child has a low risk of having RRP.")
                            Text("It's unlikely your child needs any medical assistance, but if you still have concerns visit the Medical help page on the app.")
                        }
                    } else {
                        Text("Invalid diagnosis value: $diagnosis")
                    }
                }
            }
        )
    }
}
