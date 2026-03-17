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
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.SmartVoiceOutlinedTextFieldColors
import com.example.smartvoice.ui.theme.White

@Composable
fun LoginScreen(
    navigateToScreenOption: (String) -> Unit,
    navigateToSignup: () -> Unit,
    application: SmartVoiceApplication,
    database: SmartVoiceDatabase,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory(application))
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSavedUsername(context)
    }

    Scaffold(backgroundColor = androidx.compose.ui.graphics.Color.Transparent) { innerPadding ->
        GradientBackground {
            LoginBody(
                onLoginSuccess = { navigateToScreenOption("home") },
                onRegisterClick = navigateToSignup,
                viewModel = viewModel,
                modifier = modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EmailLoginField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
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
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState.savedUsername) {
        if (uiState.savedUsername != null) {
            val savedUsername = uiState.savedUsername
            if (savedUsername != null) {
                email = savedUsername
            }
            rememberMe = true
        }
    }

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

            EmailLoginField(
                email = email,
                onEmailChange = {
                    email = it
                    emailError = ""
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

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BrightBlue
                    )
                )
                Text(
                    text = "Remember Me",
                    modifier = Modifier.padding(start = 4.dp),
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.75f)
                )
            }

            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
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
                    val emailTrimmed = email.trim()

                    if (emailTrimmed.isBlank()) {
                        emailError = "Enter an email"
                        return@Button
                    }

                    viewModel.loginUser(
                        context = context,
                        emailInput = emailTrimmed,
                        password = password,
                        rememberMe = rememberMe
                    ) { isSuccess ->
                        if (isSuccess) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid email or password. Please try again."
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
                Text(
                    text = "Login",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
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
                Text(
                    text = "Sign up",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
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