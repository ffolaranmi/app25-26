package com.example.smartvoice.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.DiagnosisTable
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.supabase.AuthRepository
import com.example.smartvoice.data.supabase.SupabaseClientProvider
import com.example.smartvoice.data.supabase.SupabaseDiagnosisRemoteRepository
import com.example.smartvoice.data.supabase.SupabaseUserRow
import com.example.smartvoice.data.supabase.SupabaseUserRemoteRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LoginUiState(
    val savedUsername: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    private val authRepository = AuthRepository()
    private val remoteUserRepo = SupabaseUserRemoteRepository()
    private val remoteDiagnosisRepo = SupabaseDiagnosisRemoteRepository()

    private val uiDateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loadSavedUsername(context: Context) {
        val savedUsername = SessionPrefs.getRememberedUsername(context)
        _uiState.value = _uiState.value.copy(
            savedUsername = savedUsername
        )
    }

    fun loginUser(
        context: Context,
        emailInput: String,
        password: String,
        rememberMe: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val emailTrimmed = emailInput.trim()

            // Prefer a local profile lookup by email for fast sign-in,
            // but always rely on Supabase Auth as the password source
            val localUserByEmail = database.userDao().getUserByEmail(emailTrimmed)

            val loginSucceededLocally = if (localUserByEmail != null) {
                try {
                    authRepository.signIn(emailTrimmed, password)
                    true
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }

            if (loginSucceededLocally && localUserByEmail != null) {
                // Ensure local data reflects the latest state from Supabase
                // even when we already have a local user row.
                val client = SupabaseClientProvider.client
                val authUser = client.auth.currentUserOrNull()
                if (authUser != null) {
                    syncRemoteDataToLocal(localUserByEmail.id, authUser.id.toString())
                }

                if (rememberMe) {
                    SessionPrefs.setRememberedUsername(context, emailTrimmed)
                } else {
                    SessionPrefs.clearRememberedUsername(context)
                }

                SessionPrefs.setLoggedInUsername(context, localUserByEmail.username)
                SessionPrefs.setLoggedInUserId(context, localUserByEmail.id)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(true)
            } else {
                val remoteSuccess = tryRemoteLoginAndSyncByEmail(
                    context = context,
                    email = emailTrimmed,
                    password = password,
                    rememberMe = rememberMe
                )

                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(remoteSuccess)
            }
        }
    }

    private suspend fun tryRemoteLoginAndSyncByEmail(
        context: Context,
        email: String,
        password: String,
        rememberMe: Boolean
    ): Boolean {
        return try {
            val client = SupabaseClientProvider.client

            // sign into Supabase Auth using email/password so that
            // RLS-protected tables like public.users are accessible.
            authRepository.signIn(email, password)

            val authUser = client.auth.currentUserOrNull() ?: return false

            // Fetch the public.users profile for this authenticated user id.
            val userResult = client.postgrest["users"].select {
                filter { eq("id", authUser.id.toString()) }
                limit(1)
            }
            val remoteUser = userResult.decodeList<SupabaseUserRow>().firstOrNull()

            // Create or update the matching local Room user row.
            val existingLocalUser = database.userDao().getUserByEmail(email)
            val userToPersist = (existingLocalUser ?: User()).copy(
                firstName = remoteUser?.firstName ?: "",
                lastName = remoteUser?.lastName ?: "",
                username = remoteUser?.username ?: existingLocalUser?.username.orEmpty(),
                email = email,
                phone = remoteUser?.phone ?: existingLocalUser?.phone.orEmpty(),
                password = password,
                firstLoginFlag = remoteUser?.firstLoginFlag ?: existingLocalUser?.firstLoginFlag ?: true,
                preferredName = remoteUser?.preferredName ?: existingLocalUser?.preferredName.orEmpty()
            )

            val savedUserId = if (existingLocalUser == null) {
                database.userDao().insert(userToPersist)
                database.userDao().getUserByEmail(email)?.id ?: return false
            } else {
                database.userDao().update(userToPersist.copy(id = existingLocalUser.id))
                existingLocalUser.id
            }

            // Sync diagnoses from Supabase into local Room.
            val remoteUserId = authUser.id.toString()
            syncRemoteDataToLocal(savedUserId, remoteUserId)

            if (rememberMe) {
                SessionPrefs.setRememberedUsername(context, email)
            } else {
                SessionPrefs.clearRememberedUsername(context)
            }
            SessionPrefs.setLoggedInUsername(context, userToPersist.username.ifBlank { email })
            SessionPrefs.setLoggedInUserId(context, savedUserId)

            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun syncRemoteDataToLocal(localUserId: Long, remoteUserId: String) {
        // to avoid leaking diagnoses between accounts on a shared
        // device, clear the local diagnosis table and repopulate it only
        // with the remote diagnoses for the currently logged-in Supabase user.
        val remoteDiagnoses = remoteDiagnosisRepo.fetchDiagnosesForUser(remoteUserId)

        // Preserve local audio paths for recordings that exist on this device.
        val localAudioPathByKey: Map<Pair<String, String>, String> =
            database.diagnosisDao().getAllEntities()
                .asSequence()
                .filter { it.recordingPath.isNotBlank() }
                .mapNotNull { local ->
                    val f = File(local.recordingPath)
                    if (f.exists() && f.length() > 0) {
                        (local.patientName to local.recordingDate) to local.recordingPath
                    } else {
                        null
                    }
                }
                .toMap()

        database.diagnosisDao().clearAllDiagnoses()
        for (diagRow in remoteDiagnoses) {
            val uiRecordingDate = diagRow.recordingDate.toUiDateTimeOrOriginal()
            val restoredPath =
                localAudioPathByKey[diagRow.patientName to uiRecordingDate].orEmpty()

            val diagnosisEntity = DiagnosisTable(
                patientName = diagRow.patientName,
                diagnosis = diagRow.diagnosis,
                recordingDate = uiRecordingDate,
                recordingLength = diagRow.recordingLength ?: "",
                // Restore local path if this device has the audio file.
                recordingPath = restoredPath,
                isViewed = diagRow.isViewed
            )
            database.diagnosisDao().insertNewDiagnosis(diagnosisEntity)
        }
    }

    private fun String.toUiDateTimeOrOriginal(): String {
        // If Supabase returns ISO-8601 timestamps, convert them to the UI format
        return try {
            val instant = Instant.parse(this)
            uiDateTimeFormatter
                .withZone(ZoneId.systemDefault())
                .format(instant)
        } catch (_: Exception) {
            this
        }
    }
}