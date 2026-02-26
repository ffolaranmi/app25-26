package com.example.smartvoice

import android.app.Application
import com.example.smartvoice.data.AppContainer
import com.example.smartvoice.data.AppDataContainer
import com.example.smartvoice.data.SmartVoiceDatabase

class SmartVoiceApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
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
        container = AppDataContainer(this)
    }
}
