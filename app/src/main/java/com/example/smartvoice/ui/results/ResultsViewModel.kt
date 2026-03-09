package com.example.smartvoice.ui.results

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.network.ApiClient
import com.example.smartvoice.network.fileToMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ResultsViewModel(
    private val db: SmartVoiceDatabase,
    private val context: Context
) : ViewModel() {

    private val _diagnoses = MutableStateFlow<List<DiagnosisTable>>(emptyList())
    val diagnoses: StateFlow<List<DiagnosisTable>> = _diagnoses

    private val _unviewedCount = MutableStateFlow(0)
    val unviewedCount: StateFlow<Int> = _unviewedCount

    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        setupConnectivityMonitoring()
    }

    private fun setupConnectivityMonitoring() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                viewModelScope.launch {
                    _isServerConnected.value = isServerAccessible()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                viewModelScope.launch {
                    _isServerConnected.value = false
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                viewModelScope.launch {
                    _isServerConnected.value = isServerAccessible()
                }
            }
        }

        try {
            connectivityManager?.registerNetworkCallback(
                android.net.NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                networkCallback!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelScope.launch {
            _isServerConnected.value = isServerAccessible()
        }
    }

    private suspend fun isServerAccessible(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@withContext false
                }

                val response = ApiClient.api.predict(fileToMultipart(File(context.cacheDir, "dummy.wav")))

                android.util.Log.d("ResultsViewModel", "Server health check - response code: ${response.code()}")
                true
            } catch (e: Exception) {
                android.util.Log.d("ResultsViewModel", "Server health check failed: ${e.message}")
                false
            }
        }
    }

    fun loadDiagnoses() {
        viewModelScope.launch {
            _diagnoses.value = withContext(Dispatchers.IO) {
                db.diagnosisDao().getAllEntities()
            }
            updateUnviewedCount()
        }
    }

    fun deleteDiagnosis(diagnosis: DiagnosisTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.diagnosisDao().delete(diagnosis) }
            loadDiagnoses()
        }
    }

    fun clearAllDiagnoses() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.diagnosisDao().clearAllDiagnoses() }
            loadDiagnoses()
        }
    }

    fun markAsViewed(diagnosisId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.diagnosisDao().markAsViewed(diagnosisId) }
            loadDiagnoses()
        }
    }

    fun retryFailedAnalysis(diagnosis: DiagnosisTable) {
        viewModelScope.launch {
            android.util.Log.d("ResultsViewModel", "Starting retry for diagnosis: ${diagnosis.patientName}")

            withContext(Dispatchers.IO) {
                db.diagnosisDao().update(
                    diagnosis.copy(diagnosis = "processing")
                )
            }
            loadDiagnoses()

            val recordingFile = if (diagnosis.recordingPath.isNotEmpty()) {
                File(diagnosis.recordingPath)
            } else {
                null
            }

            android.util.Log.d("ResultsViewModel", "Recording path: ${diagnosis.recordingPath}, exists: ${recordingFile?.exists()}")

            if (recordingFile == null || !recordingFile.exists()) {

                android.util.Log.e("ResultsViewModel", "Recording file not found: ${diagnosis.recordingPath}")
                withContext(Dispatchers.IO) {
                    db.diagnosisDao().update(
                        diagnosis.copy(diagnosis = "error|0.00")
                    )
                }
                loadDiagnoses()
                return@launch
            }

            try {
                android.util.Log.d("ResultsViewModel", "Creating multipart for file: ${recordingFile.absolutePath} (${recordingFile.length()} bytes)")
                val multipartBody = fileToMultipart(recordingFile)

                android.util.Log.d("ResultsViewModel", "Sending prediction request to server...")
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.predict(multipartBody)
                }

                android.util.Log.d("ResultsViewModel", "Server response code: ${response.code()}")

                val resultString = if (response.isSuccessful && response.body() != null) {
                    val predictResponse = response.body()!!
                    val result = "${predictResponse.pathology}|${predictResponse.p_pathology}"
                    android.util.Log.d("ResultsViewModel", "Prediction successful: $result")
                    result
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    android.util.Log.e("ResultsViewModel", "Server error: ${response.code()} - $error")
                    "error|0.00"
                }

                withContext(Dispatchers.IO) {
                    db.diagnosisDao().update(
                        diagnosis.copy(diagnosis = resultString)
                    )
                }

                loadDiagnoses()
            } catch (e: Exception) {
                android.util.Log.e("ResultsViewModel", "Retry failed with exception: ${e.javaClass.simpleName} - ${e.message}", e)

                withContext(Dispatchers.IO) {
                    db.diagnosisDao().update(
                        diagnosis.copy(diagnosis = "error|0.00")
                    )
                }
                loadDiagnoses()
            }
        }
    }

    private fun updateUnviewedCount() {
        viewModelScope.launch {
            _unviewedCount.value = withContext(Dispatchers.IO) {
                db.diagnosisDao().getUnviewedCount()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            if (networkCallback != null) {
                connectivityManager?.unregisterNetworkCallback(networkCallback!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class ResultsViewModelFactory(
    private val db: SmartVoiceDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ResultsViewModel(db, context) as T
    }
}