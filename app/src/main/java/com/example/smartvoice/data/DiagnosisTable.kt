package com.example.smartvoice.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(tableName = "diagnosis",
)
data class DiagnosisTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientchi: String,
    val patientName: String,
    val diagnosis: String,
    val recordingDate: String,
    val recordingLength: String
    )