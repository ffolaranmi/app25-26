package com.example.smartvoice.ui.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.SmartVoiceApplication
import com.example.smartvoice.ui.AppViewModelProvider
import com.example.smartvoice.ui.components.SmartVoiceTopBar
import com.example.smartvoice.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val RequiredRed = Color(0xFFDC2626)

private fun isValidNameChar(c: Char) = c.isLetter() || c == ' ' || c == '\'' || c == '-'

private fun filterAndCapitaliseName(input: String): String {
    val filtered = input.filter { isValidNameChar(it) }
    return buildString {
        filtered.forEachIndexed { i, c ->
            append(
                if (i == 0 || filtered[i - 1] == ' ' || filtered[i - 1] == '-' || filtered[i - 1] == '\'') {
                    c.uppercaseChar()
                } else {
                    c.lowercaseChar()
                }
            )
        }
    }
}

private fun toSentenceCase(value: String): String = filterAndCapitaliseName(value.trim())

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
        if (password.length < 8) missing.add("8+ characters")
        if (!Regex(".*[^A-Za-z0-9].*").matches(password)) missing.add("symbol")
        return if (missing.isEmpty()) Pair(true, "") else Pair(false, "Password must include: ${missing.joinToString(", ")}")
    }
    val missing = mutableListOf<String>()
    if (password.length < 8) missing.add("8+ chars")
    if (!Regex(".*[a-z].*").matches(password)) missing.add("lowercase letter")
    if (!Regex(".*[A-Z].*").matches(password)) missing.add("uppercase letter")
    if (!Regex(".*\\d.*").matches(password)) missing.add("number")
    if (!Regex(".*[^A-Za-z0-9].*").matches(password)) missing.add("symbol")
    return if (missing.isEmpty()) Pair(true, "") else Pair(false, "Password must include: ${missing.joinToString(", ")}")
}

private fun generateUsernameSuggestions(base: String): List<String> {
    val clean = base.trimStart('@').take(10)
    return if (clean.isBlank()) {
        listOf("parent2", "parent10", "parent_1", "guardian_1")
    } else {
        listOf("${clean}2", "${clean}10", "${clean}_1", "${clean}${clean.last()}", "${clean}_")
            .distinct()
            .take(3)
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
    viewModel: SignupViewModel = viewModel(factory = AppViewModelProvider.Factory(application))
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var ukPhoneDigits by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usernameCore by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }

    var useGeneratedPassword by remember { mutableStateOf(false) }
    var generatedPassword by remember { mutableStateOf(PasswordGenerator.generatePassword()) }
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
    var declarationError by remember { mutableStateOf("") }

    var isAdultConfirmed by remember { mutableStateOf(false) }

    var generalError by remember { mutableStateOf("") }
    var showAccountCreatedDialog by remember { mutableStateOf(false) }
    var usernameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val allFieldsFilled =
        firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                ukPhoneDigits.isNotBlank() &&
                email.isNotBlank() &&
                usernameCore.isNotBlank() &&
                (if (useGeneratedPassword) generatedPassword.isNotBlank() else password.text.isNotBlank()) &&
                confirmPassword.text.isNotBlank() &&
                isAdultConfirmed

    fun validateAll(): Boolean {
        firstNameError = ""
        lastNameError = ""
        phoneError = ""
        emailError = ""
        usernameError = ""
        passwordError = ""
        confirmPasswordError = ""
        declarationError = ""
        generalError = ""
        usernameSuggestions = emptyList()

        var ok = true

        if (firstName.isBlank()) {
            firstNameError = "Required"
            ok = false
        }

        if (lastName.isBlank()) {
            lastNameError = "Required"
            ok = false
        }

        if (ukPhoneDigits.isBlank()) {
            phoneError = "Required"
            ok = false
        } else if (ukPhoneDigits.length != 10) {
            phoneError = "Must be 10 digits"
            ok = false
        }

        val emailTrimmed = email.trim()
        if (emailTrimmed.isBlank()) {
            emailError = "Required"
            ok = false
        } else {
            val (emailValid, emailMsg) = isEmailValid(emailTrimmed)
            if (!emailValid) {
                emailError = emailMsg
                ok = false
            }
        }

        val usernameTrimmed = usernameCore.trim()
        if (usernameTrimmed.isBlank()) {
            usernameError = "Required"
            ok = false
        } else {
            val (usernameValid, usernameMsg) = isUsernameValid(usernameTrimmed)
            if (!usernameValid) {
                usernameError = usernameMsg
                ok = false
            }
        }

        val passwordToValidate = if (useGeneratedPassword) generatedPassword else password.text
        if (passwordToValidate.isBlank()) {
            passwordError = "Required"
            ok = false
        } else {
            val (passwordValid, passwordMsg) = isPasswordValid(passwordToValidate, useGeneratedPassword)
            if (!passwordValid) {
                passwordError = passwordMsg
                ok = false
            }
        }

        if (confirmPassword.text.isBlank()) {
            confirmPasswordError = "Required"
            ok = false
        } else if (confirmPassword.text != passwordToValidate) {
            confirmPasswordError = "Passwords don't match"
            ok = false
        }

        if (!isAdultConfirmed) {
            declarationError = "Please confirm that you are over 18 and responsible for the child."
            ok = false
        }

        return ok
    }

    fun clearAllFields() {
        firstName = ""
        preferredName = ""
        lastName = ""
        ukPhoneDigits = ""
        email = ""
        usernameCore = ""
        password = TextFieldValue("")
        confirmPassword = TextFieldValue("")
        useGeneratedPassword = false
        generatedPassword = PasswordGenerator.generatePassword()
        passwordVisible = false
        confirmPasswordVisible = false
        submitAttempted = false
        isAdultConfirmed = false

        firstNameError = ""
        lastNameError = ""
        phoneError = ""
        emailError = ""
        usernameError = ""
        passwordError = ""
        confirmPasswordError = ""
        declarationError = ""
        generalError = ""
        usernameSuggestions = emptyList()
    }

    fun handleServerError(msg: String) {
        val lower = msg.lowercase()
        generalError = ""

        if (lower.contains("email")) {
            emailError = "Email already registered"
        }

        if (lower.contains("username")) {
            usernameError = "Username already taken"
            usernameSuggestions = generateUsernameSuggestions(usernameCore)
        }

        if (lower.contains("phone")) {
            phoneError = "Phone number already registered"
        }

        if (!lower.contains("email") && !lower.contains("username") && !lower.contains("phone")) {
            generalError = msg
            scope.launch {
                delay(4000)
                generalError = ""
            }
        }
    }

    if (showAccountCreatedDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    "Account Created",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = LogoBlue
                )
            },
            text = {
                Text(
                    "Your account has been successfully created.",
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountCreatedDialog = false
                        navigateToLogin()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = BrightBlue,
                        contentColor = White
                    ),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Text("OK", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White
        )
    }

    Scaffold(backgroundColor = Color.Transparent) { padding ->
        GradientBackground {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SmartVoiceTopBar(title = "Sign up", onBack = navigateToLogin, fontSize = 28)
                    Spacer(modifier = Modifier.height(8.dp))

                    PillOutlinedField(
                        value = firstName,
                        onValueChange = {
                            firstName = filterAndCapitaliseName(it)
                            if (submitAttempted) validateAll()
                        },
                        placeholder = requiredLabel("First Name"),
                        maxLength = 50,
                        isError = submitAttempted && firstNameError.isNotEmpty()
                    )
                    if (submitAttempted && firstNameError.isNotEmpty()) CompactErrorText(firstNameError)

                    PillOutlinedField(
                        value = preferredName,
                        onValueChange = {
                            preferredName = filterAndCapitaliseName(it)
                        },
                        placeholder = buildAnnotatedString { append("Preferred First Name") },
                        maxLength = 50
                    )

                    PillOutlinedField(
                        value = lastName,
                        onValueChange = {
                            lastName = filterAndCapitaliseName(it)
                            if (submitAttempted) validateAll()
                        },
                        placeholder = requiredLabel("Last Name"),
                        maxLength = 50,
                        isError = submitAttempted && lastNameError.isNotEmpty()
                    )
                    if (submitAttempted && lastNameError.isNotEmpty()) CompactErrorText(lastNameError)

                    UkPhoneField(
                        digits = ukPhoneDigits,
                        onDigitsChange = {
                            ukPhoneDigits = it
                            if (submitAttempted) validateAll()
                        },
                        showError = submitAttempted,
                        errorText = phoneError
                    )

                    PillOutlinedField(
                        value = email,
                        onValueChange = {
                            email = it.lowercase()
                            if (submitAttempted) validateAll()
                        },
                        placeholder = requiredLabel("Email"),
                        keyboardType = KeyboardType.Email,
                        maxLength = 254,
                        isError = submitAttempted && emailError.isNotEmpty()
                    )
                    if (submitAttempted && emailError.isNotEmpty()) CompactErrorText(emailError)

                    UsernameAtField(
                        usernameCore = usernameCore,
                        onUsernameCoreChange = {
                            usernameCore = it
                            usernameSuggestions = emptyList()
                            if (submitAttempted) validateAll()
                        },
                        showError = submitAttempted,
                        errorText = usernameError
                    )

                    if (usernameSuggestions.isNotEmpty()) {
                        UsernameSuggestionsRow(
                            suggestions = usernameSuggestions,
                            onSuggestionClick = { suggestion ->
                                usernameCore = suggestion
                                usernameSuggestions = emptyList()
                                if (submitAttempted) validateAll()
                            }
                        )
                    }

                    if (!useGeneratedPassword) {
                        NoCopyPastePasswordField(
                            value = password,
                            onValueChange = { new ->
                                password = if (new.text == password.text) new else new.copy(text = new.text)
                                if (submitAttempted) validateAll()
                            },
                            placeholder = requiredLabel("Password"),
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            isError = submitAttempted && passwordError.isNotEmpty()
                        )
                        if (submitAttempted && passwordError.isNotEmpty()) CompactErrorText(passwordError)

                        PasswordOptionBubbleButton(
                            text = "Use generated password",
                            onClick = {
                                useGeneratedPassword = true
                                generatedPassword = PasswordGenerator.generatePassword()
                                if (submitAttempted) validateAll()
                            }
                        )
                    } else {
                        GeneratedPasswordDisplay(
                            password = generatedPassword,
                            onRegenerate = {
                                generatedPassword = PasswordGenerator.generatePassword()
                                if (submitAttempted) validateAll()
                            }
                        )
                        if (submitAttempted && passwordError.isNotEmpty()) CompactErrorText(passwordError)

                        PasswordOptionBubbleButton(
                            text = "Use custom password",
                            onClick = {
                                useGeneratedPassword = false
                                password = TextFieldValue("")
                                if (submitAttempted) validateAll()
                            }
                        )
                    }

                    NoCopyPastePasswordField(
                        value = confirmPassword,
                        onValueChange = { new ->
                            confirmPassword = if (new.text == confirmPassword.text) new else new.copy(text = new.text)
                            if (submitAttempted) validateAll()
                        },
                        placeholder = requiredLabel("Confirm Password"),
                        passwordVisible = confirmPasswordVisible,
                        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                        isError = submitAttempted && confirmPasswordError.isNotEmpty()
                    )
                    if (submitAttempted && confirmPasswordError.isNotEmpty()) CompactErrorText(confirmPasswordError)

                    Spacer(modifier = Modifier.height(4.dp))

                    DeclarationCheckboxCard(
                        checked = isAdultConfirmed,
                        onCheckedChange = {
                            isAdultConfirmed = it
                            if (submitAttempted) validateAll()
                        }
                    )
                    if (submitAttempted && declarationError.isNotEmpty()) CompactErrorText(declarationError)

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = RequiredRed,
                                    fontWeight = FontWeight.Bold
                                )
                            ) { append("* Required") }
                        },
                        style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedVisibility(
                        visible = generalError.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            generalError.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        enabled = allFieldsFilled,
                        onClick = {
                            submitAttempted = true
                            generalError = ""

                            val localValid = validateAll()

                            viewModel.checkSignupConflicts(
                                phone = "+44$ukPhoneDigits",
                                username = "@${usernameCore.trim().lowercase()}",
                                email = email.trim().lowercase()
                            ) { emailExists, usernameExists, phoneExists ->

                                if (emailExists) {
                                    emailError = "Email already registered"
                                }

                                if (usernameExists) {
                                    usernameError = "Username already taken"
                                    usernameSuggestions = generateUsernameSuggestions(usernameCore)
                                } else if (usernameError == "Username already taken") {
                                    usernameError = ""
                                    usernameSuggestions = emptyList()
                                }

                                if (phoneExists) {
                                    phoneError = "Phone number already registered"
                                }

                                val hasConflict = emailExists || usernameExists || phoneExists

                                if (!localValid || hasConflict) return@checkSignupConflicts

                                val finalPassword = if (useGeneratedPassword) generatedPassword else password.text

                                viewModel.signupUser(
                                    context = context,
                                    firstName = toSentenceCase(firstName),
                                    lastName = toSentenceCase(lastName),
                                    phone = "+44$ukPhoneDigits",
                                    username = "@${usernameCore.trim().lowercase()}",
                                    email = email.trim().lowercase(),
                                    password = finalPassword,
                                    preferredName = if (preferredName.isBlank()) "" else toSentenceCase(preferredName)
                                ) { success, msg ->
                                    if (success) {
                                        clearAllFields()
                                        showAccountCreatedDialog = true
                                    } else {
                                        handleServerError(msg)
                                    }
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

                    Spacer(modifier = Modifier.height(10.dp))
                    GlowingLoginLink(text = "Already have an account? Login", onClick = navigateToLogin)
                    Text(
                        "SmartVoice",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-2.5).sp,
                            fontSize = 20.sp
                        ),
                        color = LogoBlue,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeclarationCheckboxCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = BrightBlue,
                uncheckedColor = Color(0xFF6B7280),
                checkmarkColor = White
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = RequiredRed,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("* ")
                }
                append("I confirm I am over 18 ")
                append("and responsible for any children associated with my account.")
            },
            fontSize = 11.sp,
            lineHeight = 13.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp)
                .clickable { onCheckedChange(!checked) }
        )
    }
}

@Composable
private fun NoCopyPastePasswordField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: androidx.compose.ui.text.AnnotatedString,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit,
    isError: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF6B7280)

    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            if (new.text.length <= 128) {
                onValueChange(new.copy(text = new.text, selection = new.selection, composition = null))
            }
        },
        singleLine = true,
        isError = isError,
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
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onTogglePassword, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(20.dp)
                )
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
            focusedBorderColor = if (isError) MaterialTheme.colors.error else BrightBlue,
            unfocusedBorderColor = if (isError) MaterialTheme.colors.error else Color.Transparent,
            disabledBorderColor = Color.Transparent,
            placeholderColor = placeholderColor
        )
    )
}

@Composable
private fun UsernameSuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp)
    ) {
        Text(
            "Try one of these:",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrightBlue.copy(alpha = 0.08f),
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "@$suggestion",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrightBlue,
                            maxLines = 1
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
    OutlinedTextField(
        value = password,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827)
        ),
        trailingIcon = {
            IconButton(
                onClick = onRegenerate,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Generate new password",
                    tint = LogoBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = PillGrey,
            textColor = Color(0xFF111827),
            cursorColor = LogoBlue,
            focusedBorderColor = BrightBlue,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun PasswordOptionBubbleButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = BrightBlue.copy(alpha = 0.08f)
        ) {
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.5.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = LogoBlue)
            ) {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
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
    maxLength: Int = Int.MAX_VALUE,
    isError: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    val inputTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    val placeholderColor = Color(0xFF6B7280)

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = if (allowOnlyLetters) {
                newValue.filter { it.isLetter() || it == ' ' || it == '\'' || it == '-' }
            } else {
                newValue
            }

            if (filtered.length <= maxLength) onValueChange(filtered)
        },
        singleLine = true,
        isError = isError,
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
                IconButton(onClick = { onTogglePassword?.invoke() }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF374151),
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
            focusedBorderColor = if (isError) MaterialTheme.colors.error else BrightBlue,
            unfocusedBorderColor = if (isError) MaterialTheme.colors.error else Color.Transparent,
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
                buildAnnotatedString {
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
            unfocusedBorderColor = if (showError && errorText.isNotEmpty()) MaterialTheme.colors.error else Color.Transparent,
            disabledBorderColor = Color.Transparent,
            placeholderColor = placeholderColor
        )
    )

    if (showError && errorText.isNotEmpty()) CompactErrorText(errorText)
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
                buildAnnotatedString {
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
            unfocusedBorderColor = if (showError && errorText.isNotEmpty()) MaterialTheme.colors.error else Color.Transparent,
            disabledBorderColor = Color.Transparent,
            placeholderColor = placeholderColor
        )
    )

    if (showError && errorText.isNotEmpty()) CompactErrorText(errorText)
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

