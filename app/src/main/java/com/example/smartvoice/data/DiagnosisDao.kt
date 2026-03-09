package com.example.smartvoice.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiagnosisDao {
    @Insert
    suspend fun insertNewDiagnosis(entity: DiagnosisTable)

    @Query("SELECT * FROM diagnosis ORDER BY recordingDate DESC")
    suspend fun getAllEntities(): List<DiagnosisTable>

    @Query("DELETE FROM diagnosis")
    suspend fun clearAllDiagnoses()

    @Delete
    suspend fun delete(diagnosis: DiagnosisTable)

    @Update
    suspend fun update(diagnosis: DiagnosisTable)

    @Query("DELETE FROM diagnosis WHERE patientName = :patientName")
    suspend fun deleteDiagnosesForPatient(patientName: String): Int

    @Query("SELECT COUNT(*) FROM diagnosis WHERE patientName LIKE :childName || '%'")
    suspend fun getRecordingCountForChild(childName: String): Int

    @Query("SELECT COUNT(*) FROM diagnosis WHERE isViewed = 0")
    suspend fun getUnviewedCount(): Int

    @Query("UPDATE diagnosis SET isViewed = 1 WHERE id = :diagnosisId")
    suspend fun markAsViewed(diagnosisId: Long)
}