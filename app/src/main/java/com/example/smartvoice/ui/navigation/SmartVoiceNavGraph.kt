package com.example.smartvoice.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.ui.about.AboutScreen
import com.example.smartvoice.ui.account.AccountInfoScreen
import com.example.smartvoice.ui.account.AddAdultDestination
import com.example.smartvoice.ui.account.AdultDetailDestination
import com.example.smartvoice.ui.account.AdultDetailScreen
import com.example.smartvoice.ui.account.ManageAdultsDestination
import com.example.smartvoice.ui.account.ViewChildInfoDestination
import com.example.smartvoice.ui.child.ChildDetailDestination
import com.example.smartvoice.ui.child.ChildDetailScreen
import com.example.smartvoice.ui.child.ChildInfoDestination
import com.example.smartvoice.ui.child.ChildInfoScreen
import com.example.smartvoice.ui.feedback.FeedbackScreen
import com.example.smartvoice.ui.help.FindMedicalHelpScreen
import com.example.smartvoice.ui.history.HistoryScreen
import com.example.smartvoice.ui.home.AccountInfoDestination
import com.example.smartvoice.ui.home.FeedbackDestination
import com.example.smartvoice.ui.home.HomeScreen
import com.example.smartvoice.ui.login.LoginScreen
import com.example.smartvoice.ui.record.RecordScreen
import com.example.smartvoice.ui.register.RegisterScreen

@Composable
fun SmartVoiceNavHost(
    navController: NavHostController,
    application: SmartVoiceApplication,
    database: SmartVoiceDatabase,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                navigateToScreenOption = { navController.navigate("home") },
                navigateToRegister = { navController.navigate("register") },
                application = application,
                database = database
            )
        }

        composable("register") {
            RegisterScreen(
                navigateToLogin = { navController.navigate("login") },
                application = application
            )
        }

        composable("home") {
            HomeScreen(
                navigateToScreenOption = { navController.navigate(it.route) },
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                viewModelFactory = AppViewModelProvider.Factory(application)
            )
        }

        composable("history") {
            HistoryScreen(
                navigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToRecord = { navController.navigate("record") },
                viewModelFactory = AppViewModelProvider.Factory(application)
            )
        }

        composable("record") {
            RecordScreen(
                navigateToScreenOption = { navController.navigate(it.route) },
                navigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                viewModelFactory = AppViewModelProvider.Factory(application)
            )
        }

        composable("about") {
            AboutScreen(
                navigateToScreenOption = { navController.navigate(it.route) },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(AccountInfoDestination.route) {
            AccountInfoScreen(
                database = database,
                navController = navController,
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToScreenOption = { navController.navigate(it.route) }
            )
        }

        composable("findMedicalHelp") {
            FindMedicalHelpScreen(
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(ChildInfoDestination.route) {
            ChildInfoScreen(
                database = database,
                navigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToChildDetail = { childId ->
                    navController.navigate("${ChildDetailDestination.route}/$childId")
                }
            )
        }

        composable(
            route = ChildDetailDestination.routeWithArgs,
            arguments = listOf(navArgument(ChildDetailDestination.childIdArg) { type = NavType.LongType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getLong(ChildDetailDestination.childIdArg) ?: 0L

            ChildDetailScreen(
                childId = childId,
                database = database,
                navigateBack = { navController.popBackStack() },
                navigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToAllChildren = {
                    navController.navigate(ChildInfoDestination.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(FeedbackDestination.route) {
            FeedbackScreen(
                navController = navController,
                navigateBack = { navController.popBackStack() },
                navigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AddAdultDestination.route) {
            PlaceholderScreen(
                title = "Add Adult",
                onBack = { navController.popBackStack() }
            )
        }

        composable(ManageAdultsDestination.route) {
            PlaceholderScreen(
                title = "Manage Adults",
                onBack = { navController.popBackStack() }
            )
        }

        composable(ViewChildInfoDestination.route) {
            PlaceholderScreen(
                title = "View Child Info",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AdultDetailDestination.routeWithArgs,
            arguments = listOf(navArgument(AdultDetailDestination.adultIdArg) { type = NavType.LongType })
        ) { backStackEntry ->
            val adultId = backStackEntry.arguments?.getLong(AdultDetailDestination.adultIdArg) ?: 0L

            AdultDetailScreen(
                adultId = adultId,
                database = database,
                navigateBack = { navController.popBackStack() },
                navigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToAllAdults = { navController.popBackStack(AccountInfoDestination.route, inclusive = false) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}