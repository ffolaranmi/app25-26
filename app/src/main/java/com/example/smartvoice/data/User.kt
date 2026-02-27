package com.example.smartvoice.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val username: String,
    val phone: String,
    val email: String,
    val password: String
)