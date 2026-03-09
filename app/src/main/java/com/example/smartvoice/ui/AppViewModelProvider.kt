package com.example.smartvoice.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.ui.home.HomeViewModel
import com.example.smartvoice.ui.login.LoginViewModel
import com.example.smartvoice.ui.record.RecordViewModel
import com.example.smartvoice.ui.results.ResultsViewModel
import com.example.smartvoice.ui.results.ResultsViewModelFactory
import com.example.smartvoice.ui.signup.SignupViewModel

object AppViewModelProvider {
    fun Factory(application: SmartVoiceApplication): ViewModelProvider.Factory =
        viewModelFactory {
            val database = application.smartVoiceDatabase

            initializer { HomeViewModel() }
            initializer {
                ResultsViewModelFactory(database, application.applicationContext).create(ResultsViewModel::class.java)
            }
            initializer { RecordViewModel(database) }
            initializer { LoginViewModel(database) }
            initializer { SignupViewModel(database) }
        }
}

