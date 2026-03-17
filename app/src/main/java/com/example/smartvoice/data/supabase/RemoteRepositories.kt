package com.example.smartvoice.data.supabase

import android.util.Log
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.User
import com.example.smartvoice.data.VoiceSample
import io.github.jan.supabase.postgrest.postgrest

class SupabaseUserRemoteRepository {

    private val client = SupabaseClientProvider.client

    suspend fun isEmailTaken(email: String): Boolean = try {
        val result = client.postgrest["users"].select {
            filter { eq("email", email) }
            limit(1)
        }
        result.decodeList<SupabaseUserRow>().isNotEmpty()
    } catch (e: Exception) {
        Log.e("SupabaseUserRepo", "Failed email check", e)
        false
    }

    suspend fun isUsernameTaken(username: String): Boolean = try {
        val result = client.postgrest["users"].select {
            filter { eq("username", username) }
            limit(1)
        }
        result.decodeList<SupabaseUserRow>().isNotEmpty()
    } catch (e: Exception) {
        Log.e("SupabaseUserRepo", "Failed username check", e)
        false
    }

    suspend fun isPhoneTaken(phone: String): Boolean = try {
        val result = client.postgrest["users"].select {
            filter { eq("phone", phone) }
            limit(1)
        }
        result.decodeList<SupabaseUserRow>().isNotEmpty()
    } catch (e: Exception) {
        Log.e("SupabaseUserRepo", "Failed phone check", e)
        false
    }


    // Update the existing public.users row created via trigger on auth.users
    // with the full profile details collected in the app.
    suspend fun upsertUserDetails(localUser: User, remoteUserId: String) {
        val row = localUser.toSupabaseRow(remoteUserId)
        try {
            client.postgrest["users"].update(row) {
                filter { eq("id", remoteUserId) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseUserRepo", "Failed to update remote user", e)
        }
    }
}

class SupabaseChildRemoteRepository {

    private val client = SupabaseClientProvider.client

    suspend fun syncChildInsert(child: ChildTable, remoteUserId: String) {
        val row = child.toSupabaseRow(remoteUserId)
        try {
            client.postgrest["children"].insert(row)
        } catch (e: Exception) {
            Log.e("SupabaseChildRepo", "Failed to insert child", e)
        }
    }

    suspend fun syncChildUpdate(child: ChildTable, remoteUserId: String) {
        val row = child.toSupabaseRow(remoteUserId)
        try {
            client.postgrest["children"].update(row) {
                filter { eq("id", child.id) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseChildRepo", "Failed to update child", e)
        }
    }

    suspend fun syncChildDelete(childId: Long) {
        try {
            client.postgrest["children"].delete {
                filter { eq("id", childId) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseChildRepo", "Failed to delete child", e)
        }
    }

    suspend fun deleteAllChildrenForUser(remoteUserId: String) {
        try {
            client.postgrest["children"].delete {
                filter { eq("user_id", remoteUserId) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseChildRepo", "Failed to delete children for user", e)
        }
    }


    // Fetch all children rows for a given Supabase user id.
    // The caller is responsible for mapping the remote user id
    // onto a local Room user id when constructing ChildTable.

    suspend fun fetchChildrenForUser(remoteUserId: String): List<SupabaseChildRow> = try {
        val result = client.postgrest["children"].select {
            filter { eq("user_id", remoteUserId) }
        }
        result.decodeList<SupabaseChildRow>()
    } catch (e: Exception) {
        Log.e("SupabaseChildRepo", "Failed to fetch children", e)
        emptyList()
    }
}

class SupabaseDiagnosisRemoteRepository {

    private val client = SupabaseClientProvider.client

    suspend fun insertDiagnosis(diagnosis: DiagnosisTable, childId: Long?) {
        val row = diagnosis.toSupabaseRow(childId)
        try {
            client.postgrest["diagnoses"].insert(row)
        } catch (e: Exception) {
            Log.e("SupabaseDiagnosisRepo", "Failed to insert diagnosis", e)
        }
    }

    suspend fun deleteDiagnosis(id: Long) {
        try {
            client.postgrest["diagnoses"].delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDiagnosisRepo", "Failed to delete diagnosis", e)
        }
    }

    suspend fun deleteDiagnosisByMetadata(patientName: String, recordingDate: String) {
        try {
            client.postgrest["diagnoses"].delete {
                filter {
                    eq("patient_name", patientName)
                    eq("recording_date", recordingDate)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDiagnosisRepo", "Failed to delete diagnosis by metadata", e)
        }
    }

    suspend fun clearAllDiagnosesForUser(remoteUserId: String) {
        try {
            // Delete via join on children owned by user
            val childrenResult = client.postgrest["children"].select {
                filter { eq("user_id", remoteUserId) }
            }
            val children = childrenResult.decodeList<SupabaseChildRow>()
            val childIds = children.mapNotNull { it.id }
            childIds.forEach { childId ->
                client.postgrest["diagnoses"].delete {
                    filter { eq("child_id", childId) }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDiagnosisRepo", "Failed to clear diagnoses for user", e)
        }
    }

    // Fetch all diagnoses for all children that belong to the
    // given Supabase user id. Results are returned as remote
    // rows so the caller can decide how to persist them locally.

    suspend fun fetchDiagnosesForUser(remoteUserId: String): List<SupabaseDiagnosisRow> = try {
        val childrenResult = client.postgrest["children"].select {
            filter { eq("user_id", remoteUserId) }
        }
        val children = childrenResult.decodeList<SupabaseChildRow>()
        if (children.isEmpty()) {
            return emptyList()
        }

        val allDiagnoses = mutableListOf<SupabaseDiagnosisRow>()
        for (child in children) {
            val childId = child.id ?: continue
            val diagResult = client.postgrest["diagnoses"].select {
                filter { eq("child_id", childId) }
            }
            allDiagnoses += diagResult.decodeList<SupabaseDiagnosisRow>()
        }
        allDiagnoses
    } catch (e: Exception) {
        Log.e("SupabaseDiagnosisRepo", "Failed to fetch diagnoses for user", e)
        emptyList()
    }

     // Mark all diagnoses that match the given metadata as viewed.
     // This keeps the remote `is_viewed` flag in sync with the local
     // Room cache used by the results screen.
    suspend fun markAsViewedByMetadata(patientName: String, recordingDate: String) {
        try {
            client.postgrest["diagnoses"].update(
                {
                    // Only update the is_viewed flag; leave all other
                    // fields unchanged.
                    set("is_viewed", true)
                }
            ) {
                filter {
                    eq("patient_name", patientName)
                    eq("recording_date", recordingDate)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDiagnosisRepo", "Failed to mark diagnosis as viewed", e)
        }
    }
}

class SupabaseVoiceSampleRemoteRepository {

    private val client = SupabaseClientProvider.client

    suspend fun deleteVoiceSamplesForChild(childId: Long) {
        try {
            client.postgrest["voice_samples"].delete {
                filter { eq("child_id", childId) }
            }
        } catch (e: Exception) {
            Log.e("SupabaseVoiceSampleRepo", "Failed to delete voice samples for child", e)
        }
    }

    suspend fun deleteVoiceSamplesForUser(remoteUserId: String) {
        try {
            val childrenResult = client.postgrest["children"].select {
                filter { eq("user_id", remoteUserId) }
            }
            val children = childrenResult.decodeList<SupabaseChildRow>()
            val childIds = children.mapNotNull { it.id }
            childIds.forEach { id ->
                deleteVoiceSamplesForChild(id)
            }
        } catch (e: Exception) {
            Log.e("SupabaseVoiceSampleRepo", "Failed to delete voice samples for user", e)
        }
    }

    suspend fun insertVoiceSample(sample: VoiceSample) {
        val row = sample.toSupabaseRow()
        try {
            client.postgrest["voice_samples"].insert(row)
        } catch (e: Exception) {
            Log.e("SupabaseVoiceSampleRepo", "Failed to insert voice sample", e)
        }
    }
}

