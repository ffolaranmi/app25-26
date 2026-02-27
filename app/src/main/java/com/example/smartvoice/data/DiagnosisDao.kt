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

    @Query("DELETE FROM diagnosis WHERE patientchi = :patientChi")
    suspend fun deleteDiagnosesForPatient(patientChi: String): Int
}