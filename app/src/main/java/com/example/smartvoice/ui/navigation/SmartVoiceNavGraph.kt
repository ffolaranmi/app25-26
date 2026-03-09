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
import com.example.smartvoice.ui.account.AccountInfoDestination
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
import com.example.smartvoice.ui.faqs.FaqsDestination
import com.example.smartvoice.ui.faqs.FaqsScreen
import com.example.smartvoice.ui.feedback.FeedbackDestination
import com.example.smartvoice.ui.home.HomeDestination
import com.example.smartvoice.ui.home.HomeScreen
import com.example.smartvoice.ui.login.LoginScreen
import com.example.smartvoice.ui.record.RecordDestination
import com.example.smartvoice.ui.record.RecordScreen
import com.example.smartvoice.ui.results.ResultsDestination
import com.example.smartvoice.ui.results.ResultsScreen
import com.example.smartvoice.ui.signup.SignupScreen

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
                navigateToScreenOption = { navController.navigate(HomeDestination.route) },
                navigateToSignup = { navController.navigate("signup") },
                application = application,
                database = database
            )
        }

        composable("signup") {
            SignupScreen(
                navigateToLogin = { navController.navigate("login") },
                application = application
            )
        }

        composable(HomeDestination.route) {
            HomeScreen(
                navigateToScreenOption = { navController.navigate(it.route) },
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(HomeDestination.route) { inclusive = true }
                    }
                },
                viewModelFactory = AppViewModelProvider.Factory(application),
                database = database
            )
        }

        composable(ResultsDestination.route) {
            ResultsScreen(
                navigateBack = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToRecord = { navController.navigate(RecordDestination.route) },
                viewModelFactory = AppViewModelProvider.Factory(application)
            )
        }

        composable(RecordDestination.route) {
            RecordScreen(
                navigateToScreenOption = { navController.navigate(it.route) },
                navigateBack = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                viewModelFactory = AppViewModelProvider.Factory(application),
                database = database
            )
        }

        composable(FaqsDestination.route) {
            FaqsScreen(
                navigateToHome = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AccountInfoDestination.route) {
            AccountInfoScreen(
                database = database,
                navController = navController,
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(HomeDestination.route) { inclusive = true }
                    }
                },
                navigateToHome = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navigateToScreenOption = { navController.navigate(it.route) }
            )
        }

        composable(ChildInfoDestination.route) {
            ChildInfoScreen(
                database = database,
                navigateBack = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
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
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
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
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
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
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = false }
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