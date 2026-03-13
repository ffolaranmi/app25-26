package com.example.smartvoice.data.supabase

import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.User
import com.example.smartvoice.data.VoiceSample
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUserRow(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val username: String,
    val email: String,
    val phone: String? = null,
    @SerialName("first_login_flag") val firstLoginFlag: Boolean = true,
    @SerialName("preferred_name") val preferredName: String? = null
)

@Serializable
data class SupabaseChildRow(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val gender: String,
    @SerialName("birth_month") val birthMonth: Int,
    @SerialName("birth_year") val birthYear: Int,
    @SerialName("hospital_id") val hospitalId: String? = null
)

@Serializable
data class SupabaseDiagnosisRow(
    val id: Long? = null,
    @SerialName("child_id") val childId: Long? = null,
    @SerialName("patient_name") val patientName: String,
    val diagnosis: String,
    @SerialName("recording_date") val recordingDate: String,
    @SerialName("recording_length") val recordingLength: String? = null,
    @SerialName("recording_path") val recordingPath: String? = null,
    @SerialName("is_viewed") val isViewed: Boolean = false
)

@Serializable
data class SupabaseVoiceSampleRow(
    val id: Long? = null,
    @SerialName("child_id") val childId: Long,
    @SerialName("file_location") val fileLocation: String,
    @SerialName("created_at") val createdAt: Long,
    val classification: String
)

fun User.toSupabaseRow(remoteUserId: String): SupabaseUserRow =
    SupabaseUserRow(
        id = remoteUserId,
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        phone = phone.ifBlank { null },
        firstLoginFlag = firstLoginFlag,
        preferredName = preferredName.ifBlank { null }
    )

fun ChildTable.toSupabaseRow(remoteUserId: String): SupabaseChildRow =
    SupabaseChildRow(
        userId = remoteUserId,
        firstName = firstName,
        lastName = lastName,
        gender = gender,
        birthMonth = birthMonth,
        birthYear = birthYear,
        hospitalId = hospitalId.ifBlank { null }
    )

fun DiagnosisTable.toSupabaseRow(childId: Long?): SupabaseDiagnosisRow =
    SupabaseDiagnosisRow(
        childId = childId,
        patientName = patientName,
        diagnosis = diagnosis,
        recordingDate = recordingDate,
        recordingLength = recordingLength,
        recordingPath = recordingPath.ifBlank { null },
        isViewed = isViewed
    )

fun VoiceSample.toSupabaseRow(): SupabaseVoiceSampleRow =
    SupabaseVoiceSampleRow(
        childId = childId,
        fileLocation = fileLocation,
        createdAt = createdAt,
        classification = classification.name
    )

