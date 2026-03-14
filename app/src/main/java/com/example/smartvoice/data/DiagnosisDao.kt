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

    @Query("SELECT * FROM diagnosis WHERE userId = :userId ORDER BY recordingDate DESC")
    suspend fun getAllEntities(userId: Long): List<DiagnosisTable>

    @Query("DELETE FROM diagnosis WHERE userId = :userId")
    suspend fun clearAllDiagnoses(userId: Long)

    @Delete
    suspend fun delete(diagnosis: DiagnosisTable)

    @Update
    suspend fun update(diagnosis: DiagnosisTable)

    @Query("DELETE FROM diagnosis WHERE patientName = :patientName AND userId = :userId")
    suspend fun deleteDiagnosesForPatient(patientName: String, userId: Long): Int

    @Query("SELECT COUNT(*) FROM diagnosis WHERE patientName LIKE :childName || '%' AND userId = :userId")
    suspend fun getRecordingCountForChild(childName: String, userId: Long): Int

    @Query("SELECT COUNT(*) FROM diagnosis WHERE isViewed = 0 AND userId = :userId")
    suspend fun getUnviewedCount(userId: Long): Int

    @Query("UPDATE diagnosis SET isViewed = 1 WHERE id = :diagnosisId")
    suspend fun markAsViewed(diagnosisId: Long)
}