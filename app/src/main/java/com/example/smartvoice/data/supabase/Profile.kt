package com.example.smartvoice.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("patient_name") val patientName: String,
    val age: Int? = null,
    val chinum: Long? = null,
    @SerialName("parent_name") val parentName: String? = null,
    val email: String
)