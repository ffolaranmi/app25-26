package com.example.smartvoice.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioPlayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}