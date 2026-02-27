package com.example.smartvoice.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import kotlinx.coroutines.launch

class RegisterViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    fun registerUser(
        firstName: String,
        lastName: String,
        phone: String,
        username: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        if (
            firstName.isBlank() ||
            lastName.isBlank() ||
            phone.isBlank() ||
            username.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            Log.e("RegisterViewModel", "Error: One or more fields are empty.")
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val emailTrimmed    = email.trim()
                val usernameTrimmed = username.trim()

                val emailExists    = database.userDao().checkIfEmailExists(emailTrimmed) > 0
                val usernameExists = database.userDao().checkIfUsernameExists(usernameTrimmed) > 0

                if (emailExists || usernameExists) {
                    Log.e("RegisterViewModel", "Error: Email or username already exists.")
                    onResult(false)
                    return@launch
                }

                val newUser = User(
                    firstName = firstName.trim(),
                    lastName  = lastName.trim(),
                    username  = usernameTrimmed,
                    phone     = phone.trim(),
                    email     = emailTrimmed,
                    password  = password
                )

                database.userDao().insert(newUser)
                Log.d("RegisterViewModel", "User registered successfully: $newUser")
                onResult(true)

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error registering user", e)
                onResult(false)
            }
        }
    }
}