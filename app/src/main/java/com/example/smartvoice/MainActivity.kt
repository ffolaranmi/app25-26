package com.example.smartvoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.smartvoice.ui.splash.SplashScreen
import com.example.smartvoice.ui.theme.SmartVoiceTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as SmartVoiceApplication
        val database = application.smartVoiceDatabase

        setContent {
            SmartVoiceTheme {

                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(4000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    SmartVoiceApp(application = application, database = database)
                }
            }
        }
    }
}


// btw you gotta change the gradle version to JDK 17 JetbBrains Runtime for this app to run properly