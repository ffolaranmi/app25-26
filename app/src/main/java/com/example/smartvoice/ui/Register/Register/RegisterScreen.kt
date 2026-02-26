package com.example.smartvoice.ui.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartvoice.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.data.SmartVoiceDatabase

private fun isUsernameValid(usernameCore: String) =
    Regex("^[A-Za-z0-9._]{4,12}$").matches(usernameCore)

private fun isEmailValid(email: String) =
    Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)

private fun isPasswordValid(password: String) =
    Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$").matches(password)

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit,
    application: SmartVoiceApplication,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(
        factory = AppViewModelProvider.Factory(application)
    )
){
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var ukPhoneDigits by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usernameCore by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var submitAttempted by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var accountCreatedMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val allFieldsFilled =
        firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                ukPhoneDigits.length == 9 &&
                email.isNotBlank() &&
                usernameCore.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank()

    fun validateAll(): Boolean {
        firstNameError = ""
        lastNameError = ""
        phoneError = ""
        emailError = ""
        usernameError = ""
        passwordError = ""
        confirmPasswordError = ""

        var ok = true

        if (firstName.isBlank()) { firstNameError = "Required"; ok = false }
        if (lastName.isBlank()) { lastNameError = "Required"; ok = false }
        if (ukPhoneDigits.length != 9) { phoneError = "Invalid phone"; ok = false }

        val emailTrimmed = email.trim()
        if (emailTrimmed.isBlank() || !isEmailValid(emailTrimmed)) { emailError = "Invalid email"; ok = false }

        if (usernameCore.isBlank()) { usernameError = "Required"; ok = false }
        else if (!isUsernameValid(usernameCore)) { usernameError = "Invalid username"; ok = false }

        if (password.isBlank()) { passwordError = "Required"; ok = false }
        else if (!isPasswordValid(password)) { passwordError = "Invalid password"; ok = false }

        if (confirmPassword.isBlank()) { confirmPasswordError = "Required"; ok = false }
        else if (confirmPassword != password) { confirmPasswordError = "Passwords do not match"; ok = false }

        return ok
    }

    fun clearAllFields() {
        firstName = ""
        lastName = ""
        ukPhoneDigits = ""
        email = ""
        usernameCore = ""
        password = ""
        confirmPassword = ""
        passwordVisible = false
        confirmPasswordVisible = false
        submitAttempted = false

        firstNameError = ""
        lastNameError = ""
        phoneError = ""
        emailError = ""
        usernameError = ""
        passwordError = ""
        confirmPasswordError = ""
    }

    Scaffold(backgroundColor = Color.Transparent) { padding ->
        GradientBackground  {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp)
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(54.dp))

                    Text(
                        text = "Signup",
                        style = MaterialTheme.typography.h4.copy(
                            fontSize = 34.sp,
                            letterSpacing = (-2.5).sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = LogoBlue,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    PillOutlinedField(firstName, { firstName = it }, "First Name")
                    if (submitAttempted && firstNameError.isNotEmpty()) ErrorText(firstNameError)

                    PillOutlinedField(lastName, { lastName = it }, "Last Name")
                    if (submitAttempted && lastNameError.isNotEmpty()) ErrorText(lastNameError)

                    UkPhoneField(
                        digits = ukPhoneDigits,
                        onDigitsChange = { ukPhoneDigits = it },
                        showError = submitAttempted,
                        errorText = phoneError
                    )

                    PillOutlinedField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
                    if (submitAttempted && emailError.isNotEmpty()) ErrorText(emailError)

                    UsernameAtField(
                        usernameCore = usernameCore,
                        onUsernameCoreChange = { usernameCore = it },
                        showError = submitAttempted,
                        errorText = usernameError
                    )

                    PillOutlinedField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )
                    if (submitAttempted && passwordError.isNotEmpty()) ErrorText(passwordError)

                    PillOutlinedField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
                    )
                    if (submitAttempted && confirmPasswordError.isNotEmpty()) ErrorText(confirmPasswordError)

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        enabled = allFieldsFilled,
                        onClick = {
                            accountCreatedMessage = ""
                            submitAttempted = true

                            if (!validateAll()) return@Button

                            val fullPhone = "+44$ukPhoneDigits"
                            val fullUsername = "@$usernameCore"

                            viewModel.registerUser(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                phone = fullPhone,
                                username = fullUsername,
                                email = email.trim(),
                                password = password
                            ) { success ->
                                if (success) {
                                    accountCreatedMessage = "Account created"
                                    clearAllFields()
                                    scope.launch {
                                        delay(2500)
                                        accountCreatedMessage = ""
                                    }
                                } else {
                                    accountCreatedMessage = "Could not create account"
                                }
                            }
                        },
                        modifier = Modifier.width(180.dp).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = BrightBlue,
                            contentColor = White,
                            disabledBackgroundColor = BrightBlue.copy(alpha = 0.45f),
                            disabledContentColor = White.copy(alpha = 0.9f)
                        ),
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Text("Sign up", style = MaterialTheme.typography.button)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlowingLoginLink(
                        text = "Already have an account? Login",
                        onClick = navigateToLogin
                    )

                    AnimatedVisibility(
                        visible = accountCreatedMessage.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Text(
                            text = accountCreatedMessage,
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = BrightBlue,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "SmartVoice",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-2.5).sp
                        ),
                        color = LogoBlue,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun UsernameAtField(
    usernameCore: String,
    onUsernameCoreChange: (String) -> Unit,
    showError: Boolean,
    errorText: String
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF4B5563)

    CompositionLocalProvider(LocalContentAlpha provides 1f) {
        OutlinedTextField(
            value = usernameCore,
            onValueChange = { raw ->
                val cleaned = raw.filter { it.isLetterOrDigit() || it == '.' || it == '_' }.take(12)
                onUsernameCoreChange(cleaned)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
            textStyle = inputTextStyle,
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(vertical = 4.dp),
            shape = shape,
            isError = showError && errorText.isNotEmpty(),
            leadingIcon = {
                Text(
                    text = "@",
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            placeholder = { Text("Username", color = placeholderColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = PillGrey,
                textColor = inputTextStyle.color,
                cursorColor = BrightBlue,
                focusedBorderColor = if (showError && errorText.isNotEmpty()) MaterialTheme.colors.error else BrightBlue,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                placeholderColor = placeholderColor
            )
        )
    }
    if (showError && errorText.isNotEmpty()) ErrorText(errorText)
}

@Composable
private fun UkPhoneField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    showError: Boolean,
    errorText: String
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF4B5563)

    CompositionLocalProvider(LocalContentAlpha provides 1f) {
        OutlinedTextField(
            value = digits,
            onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(9)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            textStyle = inputTextStyle,
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(vertical = 4.dp),
            shape = shape,
            isError = showError && errorText.isNotEmpty(),
            leadingIcon = {
                Text("+44", color = Color(0xFF111827), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
            },
            placeholder = { Text("Mobile Number", color = placeholderColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = PillGrey,
                textColor = inputTextStyle.color,
                cursorColor = LogoBlue,
                focusedBorderColor = if (showError && errorText.isNotEmpty()) MaterialTheme.colors.error else BrightBlue,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                placeholderColor = placeholderColor
            )
        )
    }
    if (showError && errorText.isNotEmpty()) ErrorText(errorText)
}

@Composable
private fun PillOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF4B5563)
    val iconColor = Color(0xFF374151)

    CompositionLocalProvider(LocalContentAlpha provides 1f) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder, color = placeholderColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            textStyle = inputTextStyle,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { onTogglePassword?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(vertical = 4.dp),
            shape = shape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = PillGrey,
                textColor = inputTextStyle.color,
                cursorColor = LogoBlue,
                focusedBorderColor = BrightBlue,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                placeholderColor = placeholderColor
            )
        )
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colors.error,
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 2.dp, bottom = 6.dp)
    )
}

@Composable
private fun GlowingLoginLink(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val glowColor = if (pressed) BrightBlue.copy(alpha = 0.15f) else Color.Transparent

    Surface(color = glowColor, shape = RoundedCornerShape(10.dp)) {
        TextButton(
            onClick = onClick,
            interactionSource = interactionSource,
            colors = ButtonDefaults.textButtonColors(contentColor = GreyDark)
        ) {
            Text(text, style = MaterialTheme.typography.body2)
        }
    }
}