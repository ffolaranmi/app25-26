package com.example.smartvoice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(diagnosisTable: DiagnosisTable)

    @Query("SELECT * FROM diagnosis WHERE patientName = :patientId ORDER BY recordingDate DESC")
    fun getRecordingsForUser(patientId: String): Flow<List<DiagnosisTable>>
}