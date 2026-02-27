package com.example.smartvoice.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.history.HistoryViewModel
import com.example.smartvoice.ui.home.HomeViewModel
import com.example.smartvoice.ui.login.LoginViewModel
import com.example.smartvoice.ui.record.RecordViewModel
import com.example.smartvoice.ui.register.RegisterViewModel

object AppViewModelProvider {
    fun Factory(application: SmartVoiceApplication): ViewModelProvider.Factory =
        viewModelFactory {
            val database = application.smartVoiceDatabase // Retrieve database inside the factory

            initializer { HomeViewModel() }
            initializer { HistoryViewModel(database) }
            initializer { RecordViewModel(database) }
            initializer { LoginViewModel(database) }
            initializer { RegisterViewModel(database) } // Pass database internally
        }
}