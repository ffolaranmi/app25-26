package com.example.smartvoice.ui.record

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import com.example.smartvoice.data.supabase.SupabaseDiagnosisRemoteRepository
import com.example.smartvoice.network.ApiClient
import com.example.smartvoice.network.fileToMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordViewModel(
    private val smartVoiceDatabase: SmartVoiceDatabase
) : ViewModel() {

    private val wavRecorder = WavRecorder()
    private var lastWavFile: File? = null
    private val remoteDiagnosisRepo = SupabaseDiagnosisRemoteRepository()

    fun getRecordingDirectory(context: Context): File {
        return File(context.filesDir, "recordings").apply {
            if (!exists()) {
                mkdirs()
                Log.d("RecordViewModel", "Created recordings directory: $absolutePath")
            }
        }
    }

    fun listAllRecordings(context: Context) {
        val recordingsDir = getRecordingDirectory(context)
        if (recordingsDir.exists()) {
            val files = recordingsDir.listFiles()
            if (files != null) {
                Log.d("RecordViewModel", "=== All Recordings ===")
                files.forEach { file ->
                    Log.d("RecordViewModel", "Found: ${file.name} (${file.length()} bytes)")
                }
                Log.d("RecordViewModel", "====================")
            } else {
                Log.d("RecordViewModel", "No files in recordings directory")
            }
        } else {
            Log.d("RecordViewModel", "Recordings directory does not exist")
        }
    }

    suspend fun startRecording(context: Context, seconds: Int = 5) {
        val currentUser = getCurrentUser() ?: run {
            Log.e("RecordViewModel", "No current user found")
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeUserTag = currentUser.username
            .removePrefix("@")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")

        val recordingsDir = getRecordingDirectory(context)
        val fileName = "${safeUserTag}_$timeStamp.wav"
        val outFile = File(recordingsDir, fileName)
        lastWavFile = outFile

        Log.d("RecordViewModel", "Starting recording to: ${outFile.absolutePath} for $seconds seconds")
        Log.d("RecordViewModel", "Recording directory: ${recordingsDir.absolutePath}")

        withContext(Dispatchers.IO) {
            try {
                wavRecorder.start(outFile, seconds = seconds)
                Log.d("RecordViewModel", "Recording completed successfully")

                if (outFile.exists()) {
                    Log.d("RecordViewModel", "✓ File created successfully: ${outFile.length()} bytes")

                    verifyWavFile(outFile)
                } else {
                    Log.e("RecordViewModel", "✗ File was not created!")
                }
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Failed to start recording", e)
                throw e
            }
        }
    }

    private fun verifyWavFile(file: File) {
        try {
            val header = ByteArray(12)
            file.inputStream().use { it.read(header) }
            val riffSig = String(header, 0, 4, Charsets.US_ASCII)
            val waveSig = String(header, 8, 4, Charsets.US_ASCII)
            Log.d("RecordViewModel", "WAV header verification - RIFF: $riffSig, WAVE: $waveSig")

            if (riffSig == "RIFF" && waveSig == "WAVE") {
                Log.d("RecordViewModel", "✓ Valid WAV file")
            } else {
                Log.e("RecordViewModel", "✗ Invalid WAV header!")
            }
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error verifying WAV file", e)
        }
    }

    fun stopRecording() {
        try {
            wavRecorder.stop()
            Log.d("RecordViewModel", "Recording stopped")

            lastWavFile?.let { file ->
                if (file.exists()) {
                    Log.d("RecordViewModel", "Recording file exists: ${file.length()} bytes")
                    verifyWavFile(file)
                } else {
                    Log.e("RecordViewModel", "Recording file was not created!")
                }
            }
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error stopping recording", e)
        }
    }

    fun getLastWavFile(): File? = lastWavFile

    /**
     * Sends the WAV file to the SmartVoice prediction server and returns a
     * compact result string in the format:  "pathology|0.72"
     *
     * - pathology  = "true" or "false"  (the boolean verdict from the server)
     * - p_pathology = probability 0.0–1.0 rounded to 2 decimal places
     *
     * On any network error the string "error|0.00" is returned so the app
     * never crashes — the history tile will show an "Error" badge instead.
     */
    suspend fun runModel(wavFile: File): String {
        if (!wavFile.exists()) {
            Log.e("RecordViewModel", "WAV file does not exist: ${wavFile.absolutePath}")
            return "error|0.00"
        }

        Log.d("RecordViewModel", "Starting model inference on file: ${wavFile.name} (${wavFile.length()} bytes)")

        return withContext(Dispatchers.IO) {
            try {
                val part = fileToMultipart(wavFile)
                Log.d("RecordViewModel", "Multipart created, sending to server...")

                val response = ApiClient.api.predict(part)
                Log.d("RecordViewModel", "Server responded with code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val prob = String.format("%.2f", body.p_pathology)
                        val result = "${body.pathology}|$prob"
                        Log.d("RecordViewModel", "✓ Model inference successful: $result")
                        result
                    } else {
                        Log.e("RecordViewModel", "✗ Server returned success (200) but empty body")
                        "error|0.00"
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("RecordViewModel", "✗ Server error ${response.code()}: ${response.message()}\nBody: $errorBody")
                    "error|0.00"
                }
            } catch (e: java.net.ConnectException) {
                Log.e("RecordViewModel", "✗ Cannot connect to server - is it running? Check your IP/port")
                "error|0.00"
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("RecordViewModel", "✗ Server request timed out - server may be slow or not responding")
                "error|0.00"
            } catch (e: Exception) {
                Log.e("RecordViewModel", "✗ Network call failed: ${e.javaClass.simpleName} - ${e.message}", e)
                "error|0.00"
            }
        }
    }

    fun insertDiagnosis(diagnosisTable: DiagnosisTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    smartVoiceDatabase.diagnosisDao().insertNewDiagnosis(diagnosisTable)
                    // Mirror into Supabase without blocking local insert
                    remoteDiagnosisRepo.insertDiagnosis(diagnosisTable, null)
                    Log.d("RecordViewModel", "Diagnosis inserted: ${diagnosisTable.patientName}")
                    Log.d("RecordViewModel", "Recording path saved: ${diagnosisTable.recordingPath}")
                } catch (e: Exception) {
                    Log.e("RecordViewModel", "Failed to insert diagnosis", e)
                }
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            try {
                val user = smartVoiceDatabase.userDao().getLatestUser()
                Log.d("RecordViewModel", "Current user: ${user?.username}")
                user
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Failed to get current user", e)
                null
            }
        }
    }
}