package com.example.smartvoice.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val savedUsername: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

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
        usernameInput: String,
        password: String,
        rememberMe: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trimmed = usernameInput.trim()
            val normalizedUsername = if (trimmed.startsWith("@")) trimmed else "@$trimmed"

            val user = database.userDao().getUserByUsernameAndPassword(normalizedUsername, password)

            if (user != null) {
                if (rememberMe) {
                    SessionPrefs.setRememberedUsername(context, normalizedUsername)
                } else {
                    SessionPrefs.clearRememberedUsername(context)
                }

                SessionPrefs.setLoggedInUsername(context, normalizedUsername)
                SessionPrefs.setLoggedInUserId(context, user.id)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(false)
            }
        }
    }
}