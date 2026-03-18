package com.example.smartvoice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvoice.ui.navigation.SmartVoiceNavHost
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.White
import com.example.smartvoice.ui.theme.RegalNavy

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun SmartVoiceApp(
    application: SmartVoiceApplication,
    database: SmartVoiceDatabase,
    navController: NavHostController = rememberNavController()
) {
    SmartVoiceNavHost(
        navController = navController,
        application = application,
        database = database
    )
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@Composable
fun SmartVoiceTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) RegalNavy else LogoBlue
    val contentColor = White

    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        backgroundColor = bgColor,
        contentColor = contentColor,
        navigationIcon = if (canNavigateBack) {
            {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        } else null
    )
}

// hello :)
// btw you gotta change the gradle version to JDK 17 JetbBrains Runtime for this app to run to run properly