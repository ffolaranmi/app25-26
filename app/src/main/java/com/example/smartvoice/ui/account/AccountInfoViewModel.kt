package com.example.smartvoice.ui.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import com.example.smartvoice.data.supabase.SupabaseChildRemoteRepository
import com.example.smartvoice.data.supabase.SupabaseDiagnosisRemoteRepository
import com.example.smartvoice.data.supabase.SupabaseVoiceSampleRemoteRepository
import com.example.smartvoice.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountInfoViewModel(
    private val database: SmartVoiceDatabase,
    private val context: Context
) : ViewModel() {

    private val remoteChildRepo = SupabaseChildRemoteRepository()
    private val remoteDiagnosisRepo = SupabaseDiagnosisRemoteRepository()
    private val remoteVoiceSampleRepo = SupabaseVoiceSampleRemoteRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError

    fun refreshLoggedInUser() {
        viewModelScope.launch {
            val loggedInUsername = SessionPrefs.getLoggedInUsername(context)

            _user.value = withContext(Dispatchers.IO) {
                if (loggedInUsername.isNullOrBlank()) null
                else database.userDao().getUserByUsername(loggedInUsername)
            }
        }
    }

    fun markFirstLoginComplete(user: User) {
        viewModelScope.launch {
            val updatedUser = user.copy(firstLoginFlag = false)
            withContext(Dispatchers.IO) {
                database.userDao().update(updatedUser)
            }
            _user.value = updatedUser
        }
    }

    fun deleteAccountWithPassword(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteError.value = null

            val currentUser = _user.value
            if (currentUser == null) {
                _deleteError.value = "No user logged in"
                _isDeleting.value = false
                onError("No user logged in")
                return@launch
            }

            val isValidPassword = withContext(Dispatchers.IO) {
                val user = database.userDao().getUserByUsernameAndPassword(
                    currentUser.username,
                    password
                )
                user != null
            }

            if (!isValidPassword) {
                _deleteError.value = "Incorrect password"
                _isDeleting.value = false
                onError("Incorrect password")
                return@launch
            }

                try {
                    withContext(Dispatchers.IO) {

                        val children = database.childDao().getChildrenForUser(currentUser.id)
                        children.forEach { child ->
                            database.diagnosisDao().deleteDiagnosesForPatient(child.id.toString())
                            database.voiceSampleDAO().deleteVoiceSamplesForChild(child.id)

                            // Remote deletions per child
                            remoteDiagnosisRepo.clearAllDiagnosesForUser(
                                SupabaseClientProvider.client.auth.currentUserOrNull()?.id?.toString()
                                    ?: ""
                            )
                            remoteVoiceSampleRepo.deleteVoiceSamplesForChild(child.id)
                        }
                        database.childDao().deleteAllChildrenForUser(currentUser.id)
                        remoteChildRepo.deleteAllChildrenForUser(
                            SupabaseClientProvider.client.auth.currentUserOrNull()?.id?.toString()
                                ?: ""
                        )

                        database.userDao().delete(currentUser)
                    }

                    SessionPrefs.clearAll(context)

                _isDeleting.value = false
                onSuccess()
            } catch (e: Exception) {
                _deleteError.value = "Failed to delete account: ${e.message}"
                _isDeleting.value = false
                onError("Failed to delete account: ${e.message}")
            }
        }
    }

    init {
        refreshLoggedInUser()
    }
}