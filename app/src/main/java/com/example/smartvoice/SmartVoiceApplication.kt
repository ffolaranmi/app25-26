package com.example.smartvoice

import android.app.Application
import com.example.smartvoice.data.SmartVoiceDatabase

class SmartVoiceApplication : Application() {

    private val lock = Any()
    private var _smartVoiceDatabase: SmartVoiceDatabase? = null
    val smartVoiceDatabase: SmartVoiceDatabase
        get() {
            synchronized(lock) {
                if (_smartVoiceDatabase == null) {
                    _smartVoiceDatabase = SmartVoiceDatabase.getInstance(this)
                }
            }
            return _smartVoiceDatabase!!
        }

    override fun onCreate() {
        super.onCreate()
    }
}
