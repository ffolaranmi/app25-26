package com.example.smartvoice.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.launch

class LoginViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    fun loginUser(
        context: Context,
        usernameInput: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val trimmed = usernameInput.trim()
            val normalizedUsername = if (trimmed.startsWith("@")) trimmed else "@$trimmed"

            val user = database.userDao().getUserByUsernameAndPassword(normalizedUsername, password)

            if (user != null) {
                SessionPrefs.setLoggedInUsername(context, normalizedUsername)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}