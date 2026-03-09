package com.example.smartvoice.ui.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import kotlinx.coroutines.launch

class SignupViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    fun signupUser(
        context: Context,
        firstName: String,
        lastName: String,
        phone: String,
        username: String,
        email: String,
        password: String,
        preferredName: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        if (
            firstName.isBlank() ||
            lastName.isBlank()  ||
            phone.isBlank()     ||
            username.isBlank()  ||
            email.isBlank()     ||
            password.isBlank()
        ) {
            Log.e("SignupViewModel", "Error: One or more required fields are empty.")
            onResult(false, "All fields are required")
            return
        }

        viewModelScope.launch {
            try {
                val emailTrimmed    = email.trim()
                val usernameTrimmed = username.trim()
                val phoneTrimmed    = phone.trim()

                val emailExists    = database.userDao().checkIfEmailExists(emailTrimmed) > 0
                val usernameExists = database.userDao().checkIfUsernameExists(usernameTrimmed) > 0
                val phoneExists    = database.userDao().checkIfPhoneExists(phoneTrimmed) > 0

                when {
                    emailExists && usernameExists && phoneExists -> {
                        Log.e("SignupViewModel", "Error: Email, username, and phone already exist.")
                        onResult(false, "Email, username, and phone number already registered")
                        return@launch
                    }
                    emailExists && usernameExists -> {
                        Log.e("SignupViewModel", "Error: Email and username already exist.")
                        onResult(false, "Email and username already taken")
                        return@launch
                    }
                    emailExists && phoneExists -> {
                        Log.e("SignupViewModel", "Error: Email and phone already exist.")
                        onResult(false, "Email and phone number already registered")
                        return@launch
                    }
                    usernameExists && phoneExists -> {
                        Log.e("SignupViewModel", "Error: Username and phone already exist.")
                        onResult(false, "Username and phone number already registered")
                        return@launch
                    }
                    usernameExists -> {
                        Log.e("SignupViewModel", "Error: Username already exists.")
                        onResult(false, "Username already taken")
                        return@launch
                    }
                    emailExists -> {
                        Log.e("SignupViewModel", "Error: Email already exists.")
                        onResult(false, "Email already registered")
                        return@launch
                    }
                    phoneExists -> {
                        Log.e("SignupViewModel", "Error: Phone already exists.")
                        onResult(false, "Phone number already registered")
                        return@launch
                    }
                }

                val newUser = User(
                    firstName     = firstName.trim(),
                    lastName      = lastName.trim(),
                    username      = usernameTrimmed,
                    phone         = phoneTrimmed,
                    email         = emailTrimmed,
                    password      = password,
                    preferredName = preferredName.trim()
                )

                database.userDao().insert(newUser)

                val insertedUser = database.userDao().getUserByUsername(usernameTrimmed)
                if (insertedUser != null) {
                    SessionPrefs.setLoggedInUsername(context, usernameTrimmed)
                    SessionPrefs.setLoggedInUserId(context, insertedUser.id)
                }

                Log.d("SignupViewModel", "User signed up successfully: $newUser")
                onResult(true, "")

            } catch (e: Exception) {
                Log.e("SignupViewModel", "Error signing up user", e)
                onResult(false, "Account creation failed. Please try again")
            }
        }
    }
}