package com.example.smartvoice.ui.record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
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

    suspend fun startRecording(context: Context) {
        val currentUser = getCurrentUser() ?: return

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val safeUserTag = currentUser.username
            .removePrefix("@")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")

        val fileName = "${safeUserTag}_$timeStamp.wav"

        val outFile = File(context.filesDir, fileName)
        lastWavFile = outFile

        withContext(Dispatchers.IO) {
            wavRecorder.start(outFile, seconds = 5)
        }
    }

    fun stopRecording() {
        wavRecorder.stop()
    }

    fun getLastWavFile(): File? = lastWavFile

    fun insertDiagnosis(diagnosisTable: DiagnosisTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                smartVoiceDatabase.diagnosisDao().insertNewDiagnosis(diagnosisTable)
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            smartVoiceDatabase.userDao().getLatestUser()
        }
    }
}