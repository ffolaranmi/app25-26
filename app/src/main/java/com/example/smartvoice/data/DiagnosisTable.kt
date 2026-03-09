package com.example.smartvoice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnosis")
data class DiagnosisTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val diagnosis: String,
    val recordingDate: String,
    val recordingLength: String,
    val recordingPath: String = "",
    val isViewed: Boolean = false
)