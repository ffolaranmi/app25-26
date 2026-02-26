package com.example.smartvoice.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.UserPreferences
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.SmartVoiceOutlinedTextFieldColors
import com.example.smartvoice.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navigateToScreenOption: (String) -> Unit,
    navigateToRegister: () -> Unit,
    application: SmartVoiceApplication,
    database: SmartVoiceDatabase,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory(application))
) {
    Scaffold(backgroundColor = androidx.compose.ui.graphics.Color.Transparent) { innerPadding ->
        GradientBackground {
            LoginBody(
                onLoginSuccess = { navigateToScreenOption("home") },
                onRegisterClick = navigateToRegister,
                viewModel = viewModel,
                modifier = modifier.padding(innerPadding)
            )
        }
    }
}

private fun isUsernameValid(usernameCore: String): Boolean {
    if (usernameCore.isBlank()) return true
    return Regex("^[A-Za-z0-9._]+$").matches(usernameCore)
}

@Composable
private fun UsernameAtLoginField(
    usernameCore: String,
    onUsernameCoreChange: (String) -> Unit
) {
    OutlinedTextField(
        value = usernameCore,
        onValueChange = { raw ->
            val cleaned = raw
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                .take(12)
            onUsernameCoreChange(cleaned)
        },
        label = { Text("Username") },
        leadingIcon = { Text("@", fontWeight = FontWeight.ExtraBold) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        colors = SmartVoiceOutlinedTextFieldColors()
    )
}

@Composable
private fun LoginBody(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var usernameCore by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SmartVoice",
                style = MaterialTheme.typography.h3.copy(
                    fontSize = 44.sp,
                    letterSpacing = (-3.0).sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = LogoBlue,
                modifier = Modifier.padding(bottom = 22.dp)
            )

            UsernameAtLoginField(
                usernameCore = usernameCore,
                onUsernameCoreChange = {
                    usernameCore = it
                    usernameError = ""
                    if (errorMessage.isNotEmpty()) errorMessage = ""
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage.isNotEmpty()) errorMessage = ""
                },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 8.dp),
                colors = SmartVoiceOutlinedTextFieldColors()
            )

            if (usernameError.isNotEmpty()) {
                Text(
                    text = usernameError,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(start = 6.dp, top = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }

            val buttonModifier = Modifier
                .width(170.dp)
                .height(48.dp)

            Button(
                onClick = {
                    val core = usernameCore.trim()

                    if (core.isBlank()) {
                        usernameError = "Enter a username"
                        return@Button
                    }

                    if (!isUsernameValid(core)) {
                        usernameError = "Invalid username"
                        return@Button
                    }

                    viewModel.loginUser(core, password) { isSuccess ->
                        if (isSuccess) {
                            coroutineScope.launch {
                                userPrefs.saveUserEmail("@$core")
                            }
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid username or password. Please try again."
                        }
                    }
                },
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BrightBlue,
                    contentColor = White
                ),
                elevation = ButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Text(text = "Login", style = MaterialTheme.typography.button)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegisterClick,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BrightBlue,
                    contentColor = White
                ),
                elevation = ButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Text(text = "Sign up", style = MaterialTheme.typography.button)
            }
        }

        Text(
            text = "University of Strathclyde 2026",
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}