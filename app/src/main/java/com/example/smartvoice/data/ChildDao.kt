package com.example.smartvoice.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChildDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildTable)

    @Update
    suspend fun updateChild(child: ChildTable)

    @Delete
    suspend fun deleteChild(child: ChildTable)

    @Query("SELECT * FROM children ORDER BY id DESC")
    suspend fun getAllChildren(): List<ChildTable>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: Long): ChildTable?

    @Query("DELETE FROM children WHERE id = :userId")
    suspend fun deleteAllChildrenForUser(userId: Long)

    @Query("SELECT * FROM children WHERE userId = :userId")
    suspend fun getChildrenForUser(userId: Long): List<ChildTable>
}