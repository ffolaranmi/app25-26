package com.example.smartvoice.ui.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val RequiredRed = Color(0xFFDC2626)

private fun isUsernameValid(usernameCore: String): Pair<Boolean, String> {
    return when {
        usernameCore.length < 4 -> Pair(false, "Min 4 characters")
        usernameCore.length > 12 -> Pair(false, "Max 12 characters")
        !Regex("^[A-Za-z0-9._]+$").matches(usernameCore) -> Pair(false, "Only letters, numbers, dots, underscores")
        else -> Pair(true, "")
    }
}

private fun isEmailValid(email: String): Pair<Boolean, String> {
    return if (Regex("^[A-Za-z0-9._@-]+@[A-Za-z0-9._-]+\\.[A-Za-z]{2,}$").matches(email.trim())) {
        Pair(true, "")
    } else {
        Pair(false, "Invalid format (name@domain.ext)")
    }
}

private fun isPasswordValid(password: String, isGenerated: Boolean = false): Pair<Boolean, String> {
    if (isGenerated) {
        val missing = mutableListOf<String>()
        if (password.length < 8) missing.add("8+ chars")
        if (!Regex(".*[^A-Za-z0-9].*").matches(password)) missing.add("special char")

        return if (missing.isEmpty()) {
            Pair(true, "")
        } else {
            Pair(false, "Need: ${missing.joinToString(", ")}")
        }
    }

    val missing = mutableListOf<String>()
    if (password.length < 8) missing.add("8+ chars")
    if (!Regex(".*[a-z].*").matches(password)) missing.add("lowercase")
    if (!Regex(".*[A-Z].*").matches(password)) missing.add("uppercase")
    if (!Regex(".*\\d.*").matches(password)) missing.add("number")
    if (!Regex(".*[^A-Za-z0-9].*").matches(password)) missing.add("special char")

    return if (missing.isEmpty()) {
        Pair(true, "")
    } else {
        Pair(false, "Need: ${missing.joinToString(", ")}")
    }
}

@Composable
private fun requiredLabel(label: String) = buildAnnotatedString {
    withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }
    append(label)
}

@Composable
fun SignupScreen(
    navigateToLogin: () -> Unit,
    application: SmartVoiceApplication,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = viewModel(
        factory = AppViewModelProvider.Factory(application)
    )
) {
    val context = LocalContext.current

    var firstName       by remember { mutableStateOf("") }
    var preferredName   by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var ukPhoneDigits   by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var usernameCore    by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var useGeneratedPassword by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf(PasswordGenerator.generatePassword()) }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var submitAttempted        by remember { mutableStateOf(false) }

    var firstNameError       by remember { mutableStateOf("") }
    var lastNameError        by remember { mutableStateOf("") }
    var phoneError           by remember { mutableStateOf("") }
    var emailError           by remember { mutableStateOf("") }
    var usernameError        by remember { mutableStateOf("") }
    var passwordError        by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var accountCreatedMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val allFieldsFilled =
        firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                ukPhoneDigits.length == 10 &&
                email.isNotBlank() &&
                usernameCore.isNotBlank() &&
                (if (useGeneratedPassword) generatedPassword.isNotBlank() else password.isNotBlank()) &&
                confirmPassword.isNotBlank()

    fun validateAll(): Boolean {
        firstNameError = ""; lastNameError = ""; phoneError = ""
        emailError = ""; usernameError = ""; passwordError = ""; confirmPasswordError = ""
        var ok = true

        if (firstName.isBlank()) { firstNameError = "Required"; ok = false }
        if (lastName.isBlank())  { lastNameError  = "Required"; ok = false }
        if (ukPhoneDigits.length != 10) { phoneError = "Must be 10 digits"; ok = false }

        val emailTrimmed = email.trim()
        val (emailValid, emailMsg) = isEmailValid(emailTrimmed)
        if (!emailValid) { emailError = emailMsg; ok = false }

        val (usernameValid, usernameMsg) = isUsernameValid(usernameCore)
        if (!usernameValid) { usernameError = usernameMsg; ok = false }

        val passwordToValidate = if (useGeneratedPassword) generatedPassword else password
        val (passwordValid, passwordMsg) = isPasswordValid(passwordToValidate, useGeneratedPassword)
        if (!passwordValid) { passwordError = passwordMsg; ok = false }

        if (confirmPassword.isBlank()) { confirmPasswordError = "Required"; ok = false }
        else if (confirmPassword != passwordToValidate) { confirmPasswordError = "Passwords don't match"; ok = false }

        return ok
    }

    fun clearAllFields() {
        firstName = ""; preferredName = ""; lastName = ""; ukPhoneDigits = ""
        email = ""; usernameCore = ""; password = ""; confirmPassword = ""
        useGeneratedPassword = true
        generatedPassword = PasswordGenerator.generatePassword()
        passwordVisible = false; confirmPasswordVisible = false; submitAttempted = false
        firstNameError = ""; lastNameError = ""; phoneError = ""; emailError = ""
        usernameError = ""; passwordError = ""; confirmPasswordError = ""
    }

    Scaffold(backgroundColor = Color.Transparent) { padding ->
        GradientBackground {
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Signup",
                        style = MaterialTheme.typography.h4.copy(
                            fontSize = 28.sp,
                            letterSpacing = (-2.5).sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = LogoBlue,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        PillOutlinedField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = requiredLabel("First Name"),
                            allowOnlyLetters = true,
                            maxLength = 50
                        )
                        if (submitAttempted && firstNameError.isNotEmpty())
                            CompactErrorText(firstNameError)


                        PillOutlinedField(
                            value = preferredName,
                            onValueChange = { preferredName = it },
                            placeholder = buildAnnotatedString { append("Preferred First Name") },
                            allowOnlyLetters = true,
                            maxLength = 50
                        )

                        PillOutlinedField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = requiredLabel("Last Name"),
                            allowOnlyLetters = true,
                            maxLength = 50
                        )
                        if (submitAttempted && lastNameError.isNotEmpty())
                            CompactErrorText(lastNameError)

                        UkPhoneField(
                            digits = ukPhoneDigits,
                            onDigitsChange = { ukPhoneDigits = it },
                            showError = submitAttempted,
                            errorText = phoneError
                        )

                        PillOutlinedField(
                            value = email,
                            onValueChange = { email = it.lowercase() },
                            placeholder = requiredLabel("Email"),
                            keyboardType = KeyboardType.Email,
                            maxLength = 254
                        )
                        if (submitAttempted && emailError.isNotEmpty())
                            CompactErrorText(emailError)

                        UsernameAtField(
                            usernameCore = usernameCore,
                            onUsernameCoreChange = { usernameCore = it },
                            showError = submitAttempted,
                            errorText = usernameError
                        )

                        if (useGeneratedPassword) {
                            GeneratedPasswordDisplay(
                                password = generatedPassword,
                                onRegenerate = { generatedPassword = PasswordGenerator.generatePassword() }
                            )
                            if (submitAttempted && passwordError.isNotEmpty())
                                CompactErrorText(passwordError)

                            TextButton(
                                onClick = { useGeneratedPassword = false; password = "" },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                Text(
                                    "Use custom password instead",
                                    fontSize = 12.sp,
                                    color = LogoBlue
                                )
                            }
                        } else {
                            PillOutlinedField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = requiredLabel("Password"),
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible }
                            )
                            if (submitAttempted && passwordError.isNotEmpty())
                                CompactErrorText(passwordError)

                            TextButton(
                                onClick = { useGeneratedPassword = true; generatedPassword = PasswordGenerator.generatePassword() },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                Text(
                                    "Use generated password instead",
                                    fontSize = 12.sp,
                                    color = LogoBlue
                                )
                            }
                        }

                        PillOutlinedField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = requiredLabel("Confirm Password"),
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                        if (submitAttempted && confirmPasswordError.isNotEmpty())
                            CompactErrorText(confirmPasswordError)

                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) {
                                    append("* Required")
                                }
                            },
                            style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            enabled = allFieldsFilled,
                            onClick = {
                                accountCreatedMessage = ""
                                submitAttempted = true
                                if (!validateAll()) return@Button

                                val finalPassword = if (useGeneratedPassword) generatedPassword else password

                                viewModel.signupUser(
                                    context       = context,
                                    firstName     = firstName.trim(),
                                    lastName      = lastName.trim(),
                                    phone         = "+44$ukPhoneDigits",
                                    username      = "@$usernameCore",
                                    email         = email.trim(),
                                    password      = finalPassword,
                                    preferredName = preferredName.trim()
                                ) { success, errorMessage ->
                                    if (success) {
                                        accountCreatedMessage = "Account created"
                                        clearAllFields()
                                        scope.launch { delay(2500); accountCreatedMessage = "" }
                                    } else {
                                        accountCreatedMessage = errorMessage.ifEmpty { "Could not create account" }
                                        scope.launch { delay(4000); accountCreatedMessage = "" }
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = BrightBlue,
                                contentColor = White,
                                disabledBackgroundColor = BrightBlue.copy(alpha = 0.45f),
                                disabledContentColor = White.copy(alpha = 0.9f)
                            ),
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Text("Sign up", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        GlowingLoginLink(
                            text = "Already have an account? Login",
                            onClick = navigateToLogin
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(
                            visible = accountCreatedMessage.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isError = accountCreatedMessage.contains("already") ||
                                    accountCreatedMessage.contains("failed") ||
                                    accountCreatedMessage.contains("Could not")
                            Text(
                                text = accountCreatedMessage.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isError) Color(0xFFDC2626) else BrightBlue,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "SmartVoice",
                            style = MaterialTheme.typography.h4.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-2.5).sp,
                                fontSize = 20.sp
                            ),
                            color = LogoBlue,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedPasswordDisplay(
    password: String,
    onRegenerate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = password,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF111827),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = onRegenerate,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Generate new password",
                    tint = LogoBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PillOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: androidx.compose.ui.text.AnnotatedString,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    allowOnlyLetters: Boolean = false,
    maxLength: Int = Int.MAX_VALUE
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF6B7280)
    val iconColor = Color(0xFF374151)

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = when {
                allowOnlyLetters -> newValue.filter { it.isLetter() || it == ' ' }
                else -> newValue
            }
            if (filtered.length <= maxLength) {
                onValueChange(filtered)
            }
        },
        singleLine = true,
        placeholder = {
            Text(
                placeholder,
                color = placeholderColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        },
        textStyle = inputTextStyle,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(
                    onClick = { onTogglePassword?.invoke() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
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

@Composable
private fun UsernameAtField(
    usernameCore: String,
    onUsernameCoreChange: (String) -> Unit,
    showError: Boolean,
    errorText: String
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF6B7280)

    OutlinedTextField(
        value = usernameCore,
        onValueChange = { raw ->
            onUsernameCoreChange(raw.filter { it.isLetterOrDigit() || it == '.' || it == '_' }.take(12))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
        textStyle = inputTextStyle,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = shape,
        isError = showError && errorText.isNotEmpty(),
        leadingIcon = {
            Text(
                "@",
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        placeholder = {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }
                    append("Username")
                },
                color = placeholderColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        },
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
    if (showError && errorText.isNotEmpty())
        CompactErrorText(errorText)
}

@Composable
private fun UkPhoneField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    showError: Boolean,
    errorText: String
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF6B7280)

    OutlinedTextField(
        value = digits,
        onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(10)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        textStyle = inputTextStyle,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = shape,
        isError = showError && errorText.isNotEmpty(),
        leadingIcon = {
            Text(
                "+44",
                color = Color(0xFF111827),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        placeholder = {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }
                    append("Mobile Number")
                },
                color = placeholderColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        },
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
    if (showError && errorText.isNotEmpty())
        CompactErrorText(errorText)
}

@Composable
private fun CompactErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colors.error,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun GlowingLoginLink(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = BrightBlue),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 13.sp)
    }
}