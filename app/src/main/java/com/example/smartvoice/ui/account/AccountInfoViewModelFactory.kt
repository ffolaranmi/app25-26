package com.example.smartvoice.ui.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartvoice.data.SmartVoiceDatabase

class AccountInfoViewModelFactory(
    private val database: SmartVoiceDatabase,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountInfoViewModel(database, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}