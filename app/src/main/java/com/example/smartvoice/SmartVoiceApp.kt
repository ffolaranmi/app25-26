package com.example.smartvoice

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvoice.ui.navigation.SmartVoiceNavHost
import com.example.smartvoice.data.SmartVoiceDatabase

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
    if (canNavigateBack) {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        )
    } else {
        TopAppBar(title = { Text(title) }, modifier = modifier)
    }
}

// hello :)
// btw you gotta change the gradle version to JDK 17 JetbBrains Runtime for this app to run properly