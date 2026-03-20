package com.example.smartvoice.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import com.example.smartvoice.ui.components.SmartVoiceTopBar

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

private fun isUsernameValid(usernameCore: String): Boolean {
    if (usernameCore.isBlank()) return true
    return Regex("^[A-Za-z0-9._]+$").matches(usernameCore)
}

@Composable
private fun UsernameAtLoginField(
    usernameCore: String,
    onUsernameCoreChange: (String) -> Unit,
    focusManager: FocusManager,
    passwordFocusRequester: FocusRequester
) {
    OutlinedTextField(
        value = usernameCore,
        onValueChange = { raw ->
            val cleaned = raw
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                .take(12)
                .lowercase()
            onUsernameCoreChange(cleaned)
        },
        label = { Text("Username") },
        leadingIcon = { Text("@", fontWeight = FontWeight.ExtraBold) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { passwordFocusRequester.requestFocus() }
        ),
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
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    var usernameCore by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState.savedUsername) {
        if (uiState.savedUsername != null) {
            val savedUsername = uiState.savedUsername
            if (savedUsername != null) {
                usernameCore = savedUsername.removePrefix("@").lowercase()
            }
            rememberMe = true
        }
    }

    fun attemptLogin() {
        val core = usernameCore.trim().lowercase()

        if (core.isBlank()) {
            usernameError = "Enter a username"
            errorMessage = ""
            return
        }

        if (!isUsernameValid(core)) {
            usernameError = "Invalid username"
            errorMessage = ""
            return
        }

        usernameError = ""

        viewModel.loginUser(
            context = context,
            usernameInput = core,
            password = password,
            rememberMe = rememberMe
        ) { isSuccess ->
            if (isSuccess) {
                onLoginSuccess()
            } else {
                errorMessage = "Invalid username or password. Please try again."
            }
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

            UsernameAtLoginField(
                usernameCore = usernameCore,
                onUsernameCoreChange = {
                    usernameCore = it
                    usernameError = ""
                    if (errorMessage.isNotEmpty()) errorMessage = ""
                },
                focusManager = focusManager,
                passwordFocusRequester = passwordFocusRequester
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage.isNotEmpty()) errorMessage = ""
                },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
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
                    .padding(vertical = 8.dp)
                    .focusRequester(passwordFocusRequester),
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
                    text = "Remember me",
                    modifier = Modifier.padding(start = 4.dp),
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.75f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(22.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (usernameError.isNotEmpty()) {
                    Text(
                        text = usernameError,
                        color = MaterialTheme.colors.error,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(26.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colors.error,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            val buttonModifier = Modifier
                .width(170.dp)
                .height(48.dp)

            Button(
                onClick = { attemptLogin() },
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