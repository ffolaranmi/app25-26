package com.example.smartvoice.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.launch

class LoginViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    fun loginUser(usernameInput: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trimmed = usernameInput.trim()

            val normalizedUsername = if (trimmed.startsWith("@")) trimmed else "@$trimmed"

            val user = database.userDao().getUserByUsernameAndPassword(normalizedUsername, password)
            onResult(user != null)
        }
    }
}