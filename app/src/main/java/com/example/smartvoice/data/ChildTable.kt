package com.example.smartvoice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class ChildTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val birthMonth: Int,
    val birthYear: Int
)