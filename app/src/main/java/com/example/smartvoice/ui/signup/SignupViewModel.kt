package com.example.smartvoice.ui.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import com.example.smartvoice.data.supabase.AuthRepository
import com.example.smartvoice.data.supabase.SupabaseUserRemoteRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignupViewModel(private val database: SmartVoiceDatabase) : ViewModel() {

    private val authRepository = AuthRepository()
    private val remoteUserRepo = SupabaseUserRemoteRepository()

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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val emailTrimmed    = email.trim()
                val usernameTrimmed = username.trim()
                val phoneTrimmed    = phone.trim()

                val localEmailExists    = database.userDao().checkIfEmailExists(emailTrimmed) > 0
                val localUsernameExists = database.userDao().checkIfUsernameExists(usernameTrimmed) > 0
                val localPhoneExists    = database.userDao().checkIfPhoneExists(phoneTrimmed) > 0

                val remoteEmailExists    = remoteUserRepo.isEmailTaken(emailTrimmed)
                val remoteUsernameExists = remoteUserRepo.isUsernameTaken(usernameTrimmed)
                val remotePhoneExists    = remoteUserRepo.isPhoneTaken(phoneTrimmed)

                val emailExists    = localEmailExists || remoteEmailExists
                val usernameExists = localUsernameExists || remoteUsernameExists
                val phoneExists    = localPhoneExists || remotePhoneExists

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

                // Create Supabase auth user
                authRepository.signUp(emailTrimmed, password)

                // Persist locally for existing flows
                database.userDao().insert(newUser)

                val insertedUser = database.userDao().getUserByUsername(usernameTrimmed)
                if (insertedUser != null) {
                    // Mirror full profile details into public.users for the current Supabase user
                    val supabaseClient = com.example.smartvoice.data.supabase.SupabaseClientProvider.client
                    val remoteUserId = supabaseClient.auth.currentUserOrNull()?.id?.toString()
                    if (remoteUserId != null) {
                        remoteUserRepo.upsertUserDetails(insertedUser, remoteUserId)
                    }

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