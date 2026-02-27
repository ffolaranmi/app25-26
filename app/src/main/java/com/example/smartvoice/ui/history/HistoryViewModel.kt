package com.example.smartvoice.ui.history

import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val smartVoiceDatabase: SmartVoiceDatabase) : ViewModel() {
    private val _diagnoses = MutableStateFlow<List<DiagnosisTable>>(emptyList())
    val diagnoses: StateFlow<List<DiagnosisTable>> = _diagnoses

    suspend fun loadDiagnoses() {
        _diagnoses.value = smartVoiceDatabase.diagnosisDao().getAllEntities()
    }

    fun clearAllDiagnoses() {
        viewModelScope.launch(Dispatchers.IO) {
            smartVoiceDatabase.diagnosisDao().clearAllDiagnoses()
            loadDiagnoses()
        }
    }

    fun deleteDiagnosis(diagnosis: DiagnosisTable) {
        viewModelScope.launch(Dispatchers.IO) {
            smartVoiceDatabase.diagnosisDao().delete(diagnosis)
            loadDiagnoses()
        }
    }

    fun renameDiagnosis(diagnosis: DiagnosisTable, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = diagnosis.copy(patientName = newName)
            smartVoiceDatabase.diagnosisDao().update(updated)
            loadDiagnoses()
        }
    }
}