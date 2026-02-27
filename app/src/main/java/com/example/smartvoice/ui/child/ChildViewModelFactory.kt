package com.example.smartvoice.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartvoice.data.SmartVoiceDatabase

class ChildViewModelFactory(
    private val db: SmartVoiceDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChildViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChildViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}