package com.example.smartvoice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceSampleDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(voiceSample: VoiceSample)

    @Update
    suspend fun update(voiceSample: VoiceSample)

    @Delete
    suspend fun delete(voiceSample: VoiceSample)

    @Query("SELECT * from voiceSample WHERE id = :id")
    fun getVoiceSample(id: Int): Flow<VoiceSample>

    @Query("SELECT * from voiceSample ORDER BY created_at ASC")
    fun getAllVoiceSamples(): Flow<List<VoiceSample>>

    @Query("DELETE FROM voiceSample WHERE childId = :childId")
    suspend fun deleteVoiceSamplesForChild(childId: Long): Int

    @Query("DELETE FROM voiceSample")
    suspend fun clearAllVoiceSamples(): Int
}