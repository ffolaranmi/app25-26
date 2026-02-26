package com.example.smartvoice.ui.account

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountInfoViewModel(
    private val database: SmartVoiceDatabase,
    private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        refreshUser()
    }

    private fun refreshUser() {
        viewModelScope.launch {
            try {
                _user.value = database.userDao().getLatestUser()
            } catch (e: Exception) {
                Log.e("AccountInfoViewModel", "Error loading user", e)
                _user.value = null
            }
        }
    }

    suspend fun deleteAccount(): Boolean {
        return try {
            val u = _user.value ?: return false
            database.userDao().delete(u)
            _user.value = null
            true
        } catch (e: Exception) {
            Log.e("AccountInfoViewModel", "Error deleting account", e)
            false
        }
    }

    suspend fun resetPassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ): Boolean {
        return try {
            val userByEmail = database.userDao().getUserByEmail(email) ?: return false
            if (userByEmail.password != currentPassword) return false

            val updated = userByEmail.copy(password = newPassword)
            database.userDao().update(updated)
            _user.value = updated
            true
        } catch (e: Exception) {
            Log.e("AccountInfoViewModel", "Error resetting password", e)
            false
        }
    }
}