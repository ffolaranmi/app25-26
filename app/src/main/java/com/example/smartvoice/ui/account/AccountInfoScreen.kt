package com.example.smartvoice.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartvoice.R
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.User
import com.example.smartvoice.ui.child.ChildInfoDestination
import com.example.smartvoice.ui.components.SmartVoiceBottomBar
import com.example.smartvoice.ui.components.SmartVoiceTopBar
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LightBlue
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object AddAdultDestination : NavigationDestination {
    override val route = "addAdult"
    override val titleRes = R.string.app_name
}

object AccountInfoDestination : NavigationDestination {
    override val route = "accountInfo"
    override val titleRes = R.string.app_name
}

object ManageAdultsDestination : NavigationDestination {
    override val route = "manageAdults"
    override val titleRes = R.string.app_name
}

object ViewChildInfoDestination : NavigationDestination {
    override val route = "viewChildInfo"
    override val titleRes = R.string.app_name
}

object AdultDetailDestination : NavigationDestination {
    override val route = "adultDetail"
    override val titleRes = R.string.app_name
    const val adultIdArg = "adultId"
    val routeWithArgs = "$route/{$adultIdArg}"
}

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private val TileTextColor = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)
private val InfoBlue = Color(0xFF1565C0)
private val RequiredRed = Color(0xFFDC2626)

private fun isValidNameChar(c: Char) = c.isLetter() || c == ' ' || c == '\'' || c == '-'

private fun filterNameInput(input: String): String {
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

@Composable
private fun requiredLabel(label: String) = buildAnnotatedString {
    withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }
    append(label)
}

class AdultsViewModel(private val db: SmartVoiceDatabase) : ViewModel() {
    private val _adults = MutableStateFlow<List<User>>(emptyList())
    val adults: StateFlow<List<User>> = _adults

    private val _selectedAdult = MutableStateFlow<User?>(null)
    val selectedAdult: StateFlow<User?> = _selectedAdult

    private val _accountHolderId = MutableStateFlow<Long?>(null)
    val accountHolderId: StateFlow<Long?> = _accountHolderId

    fun setAccountHolderId(id: Long?) { _accountHolderId.value = id }

    fun loadAdults(accountHolderId: Long) {
        viewModelScope.launch {
            val all = withContext(Dispatchers.IO) { db.userDao().getAllUsersNewestFirst() }
            _adults.value = all.filter { it.id == accountHolderId || it.accountHolderId == accountHolderId }
        }
    }

    fun loadAdultById(id: Long) {
        viewModelScope.launch {
            _selectedAdult.value = withContext(Dispatchers.IO) { db.userDao().getUserById(id) }
        }
    }

    fun updateAdult(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().update(user) }
            _selectedAdult.value = user
            _accountHolderId.value?.let { loadAdults(it) }
        }
    }

    fun addAdult(user: User, onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().insert(user) }
            _accountHolderId.value?.let { loadAdults(it) }
            onComplete()
        }
    }

    fun deleteAdult(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().delete(user) }
            _accountHolderId.value?.let { loadAdults(it) }
        }
    }
}

class AdultsViewModelFactory(private val db: SmartVoiceDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdultsViewModel(db) as T
    }
}

@Composable
fun AccountInfoScreen(
    database: SmartVoiceDatabase,
    navController: NavController,
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToScreenOption: (NavigationDestination) -> Unit
) {
    val context = LocalContext.current
    val userId = remember { SessionPrefs.getLoggedInUserId(context) }

    val accountVm: AccountInfoViewModel = viewModel(factory = AccountInfoViewModelFactory(database, context))
    val isDeleting by accountVm.isDeleting.collectAsState()
    val deleteError by accountVm.deleteError.collectAsState()

    val vm: AdultsViewModel = viewModel(factory = AdultsViewModelFactory(database))
    val adults by vm.adults.collectAsState()
    val accountHolderId by vm.accountHolderId.collectAsState()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAddAdultDialog by remember { mutableStateOf(false) }
    var showDeleteAdultDialog by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {
        if (userId != -1L) {
            vm.setAccountHolderId(userId)
            vm.loadAdults(userId)
        }
    }

    if (showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = { password ->
                accountVm.deleteAccountWithPassword(
                    password = password,
                    onSuccess = { showDeleteAccountDialog = false; navigateToLogin() },
                    onError = { }
                )
            },
            isDeleting = isDeleting,
            error = deleteError
        )
    }

    if (showAddAdultDialog) {
        AddAdultDialog(
            database = database,
            accountHolderId = userId,
            onDismiss = { showAddAdultDialog = false },
            onConfirm = { newAdult -> vm.addAdult(newAdult) { showAddAdultDialog = false } }
        )
    }

    showDeleteAdultDialog?.let { adult ->
        AlertDialog(
            onDismissRequest = { showDeleteAdultDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Remove Adult?", fontFamily = InterFont, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you sure you want to remove ${adult.preferredName.ifBlank { "${adult.firstName} ${adult.lastName}".trim() }}?", fontFamily = InterFont)
                    Text("This action cannot be undone.", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = ErrorRed)
                }
            },
            confirmButton = {
                Button(onClick = { vm.deleteAdult(adult); showDeleteAdultDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), shape = RoundedCornerShape(10.dp)) {
                    Text("Remove", fontFamily = InterFont, color = White)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteAdultDialog = null }) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) } }
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                SmartVoiceTopBar(title = "Account Info", onBack = navigateToHome)
                Spacer(modifier = Modifier.height(16.dp))

                if (adults.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrightBlue)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(adults) { adult ->
                            val isHolder = accountHolderId != null && adult.id == accountHolderId
                            AdultListTile(
                                adult = adult, isAccountHolder = isHolder,
                                onClick = { navController.navigate("${AdultDetailDestination.route}/${adult.id}") },
                                onDeleteClick = { if (!isHolder) showDeleteAdultDialog = adult }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { showAddAdultDialog = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrightBlue), shape = RoundedCornerShape(12.dp)) {
                    Text("+ Add Adult", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { navigateToScreenOption(ChildInfoDestination) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = White), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue), elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                    Text("Child Info", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = LogoBlue)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { showDeleteAccountDialog = true }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = White), shape = RoundedCornerShape(12.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                    Text("Delete Account", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                SmartVoiceBottomBar(onHomeClick = navigateToHome)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AdultListTile(adult: User, isAccountHolder: Boolean, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = PillGrey)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(adult.preferredName.ifBlank { "${adult.firstName} ${adult.lastName}".trim() }.ifEmpty { adult.username }, fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TileTextColor)
                if (isAccountHolder) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.background(color = BrightBlue.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("ACCOUNT HOLDER", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = BrightBlue, letterSpacing = 0.6.sp)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isAccountHolder) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove adult", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = LogoBlue.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun AddAdultDialog(database: SmartVoiceDatabase, accountHolderId: Long, onDismiss: () -> Unit, onConfirm: (User) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun toSentenceCase(value: String): String =
        value.trim().lowercase().replaceFirstChar { it.uppercaseChar() }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
        title = { Text("Add Adult", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LogoBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                RequiredDialogField(
                    value = firstName, label = "First Name", isError = firstNameError.isNotEmpty(),
                    onValueChange = { firstName = filterNameInput(it); firstNameError = "" },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                PlainDialogField(
                    value = preferredName, label = "Preferred First Name",
                    onValueChange = { preferredName = filterNameInput(it) },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                RequiredDialogField(
                    value = lastName, label = "Last Name", isError = lastNameError.isNotEmpty(),
                    onValueChange = { lastName = filterNameInput(it); lastNameError = "" },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (lastNameError.isNotEmpty()) FieldErrorText(lastNameError)
                UkPhoneDialogField(digits = phoneDigits, isError = phoneError.isNotEmpty(), onDigitsChange = { phoneDigits = it; phoneError = "" }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                if (phoneError.isNotEmpty()) FieldErrorText(phoneError)
                RequiredDialogField(
                    value = email, label = "Email", isError = emailError.isNotEmpty(), keyboardType = KeyboardType.Email,
                    onValueChange = { email = it.lowercase(); emailError = "" },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (emailError.isNotEmpty()) FieldErrorText(emailError)
                Spacer(modifier = Modifier.height(4.dp))
                Text(buildAnnotatedString { withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* Required") } }, fontFamily = InterFont, fontSize = 10.sp, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saving) return@Button
                    val firstTrim = firstName.trim()
                    val lastTrim = lastName.trim()
                    val phoneTrim = phoneDigits.trim()
                    val emailTrim = email.trim().lowercase()
                    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

                    firstNameError = if (firstTrim.isBlank()) "Required" else ""
                    lastNameError = if (lastTrim.isBlank()) "Required" else ""
                    phoneError = if (phoneTrim.length !in 9..10) "Must be 9 or 10 digits" else ""
                    emailError = when { emailTrim.isBlank() -> "Required"; !emailRegex.matches(emailTrim) -> "Invalid format (name@domain.ext)"; else -> "" }

                    if (firstNameError.isNotEmpty() || lastNameError.isNotEmpty() || phoneError.isNotEmpty() || emailError.isNotEmpty()) return@Button

                    saving = true
                    scope.launch {
                        val emailTaken = withContext(Dispatchers.IO) { database.userDao().checkIfEmailExists(emailTrim) > 0 }
                        val phoneTaken = withContext(Dispatchers.IO) { database.userDao().checkIfPhoneExists("+44$phoneTrim") > 0 }
                        if (emailTaken) emailError = "Email already registered"
                        if (phoneTaken) phoneError = "Phone number already registered"
                        if (emailTaken || phoneTaken) { saving = false; return@launch }

                        val placeholderUsername = "@adult_${UUID.randomUUID().toString().take(8)}"
                        val placeholderPassword = UUID.randomUUID().toString()
                        onConfirm(User(firstName = firstTrim, lastName = lastTrim, preferredName = if (preferredName.isBlank()) "" else preferredName.trim(), phone = "+44$phoneTrim", email = emailTrim, username = placeholderUsername, password = placeholderPassword, accountHolderId = accountHolderId))
                        saving = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !saving
            ) { Text(if (saving) "Saving..." else "Add", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White) }
        },
        dismissButton = { TextButton(onClick = { if (!saving) onDismiss() }) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) } }
    )
}

@Composable
private fun FieldErrorText(text: String) {
    Text(text = text, fontFamily = InterFont, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed, modifier = Modifier.fillMaxWidth().padding(start = 14.dp, bottom = 2.dp))
}

@Composable
private fun RequiredDialogField(value: String, label: String, isError: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text, keyboardActions: KeyboardActions = KeyboardActions.Default, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = if (isError) ErrorRed.copy(alpha = 0.05f) else PillGrey, shape = RoundedCornerShape(12.dp))) {
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(buildAnnotatedString { withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }; append(label) }, fontFamily = InterFont, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType, imeAction = ImeAction.Done),
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
            textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlainDialogField(value: String, label: String, keyboardActions: KeyboardActions = KeyboardActions.Default, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
            textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdultDetailScreen(adultId: Long, database: SmartVoiceDatabase, navigateBack: () -> Unit, navigateHome: () -> Unit, navigateToAllAdults: () -> Unit) {
    val context = LocalContext.current
    val accountVm: AccountInfoViewModel = viewModel(factory = AccountInfoViewModelFactory(database, context))
    val accountHolder by accountVm.user.collectAsState()
    val vm: AdultsViewModel = viewModel(factory = AdultsViewModelFactory(database))
    val adult by vm.selectedAdult.collectAsState()
    val isAccountHolder = adult != null && adult?.id == accountHolder?.id
    var showManageDialog by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(adultId) { vm.loadAdultById(adultId) }

    if (showManageDialog && adult != null) {
        EditAdultDialog(database = database, originalAdult = adult!!, isAccountHolder = isAccountHolder, onDismiss = { showManageDialog = false }, onConfirm = { updated -> vm.updateAdult(updated); showManageDialog = false })
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                SmartVoiceTopBar(title = adult?.let { it.preferredName.ifBlank { "${it.firstName} ${it.lastName}".trim() } } ?: "Adult", onBack = navigateBack)
                Spacer(modifier = Modifier.height(14.dp))

                adult?.let { a ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactInfoTile(label = "First Name", value = a.firstName, modifier = Modifier.weight(1f))
                        CompactInfoTile(label = "Last Name", value = a.lastName, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (a.preferredName.isNotBlank()) {
                        CompactInfoTile(label = "Preferred Name", value = a.preferredName, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    CompactInfoTile(label = "Mobile Number", value = a.phone, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactInfoTile(label = "Email", value = a.email, modifier = Modifier.fillMaxWidth())
                    if (isAccountHolder) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CompactInfoTile(label = "Username", value = a.username, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        CompactPasswordTile(password = a.password, isVisible = showPassword, onToggle = { showPassword = !showPassword })
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.background(color = BrightBlue.copy(alpha = 0.10f), shape = RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text("ACCOUNT HOLDER", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BrightBlue, letterSpacing = 0.6.sp)
                        }
                    }
                } ?: Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrightBlue)
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showManageDialog = true }, modifier = Modifier.width(220.dp).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = BrightBlue), shape = RoundedCornerShape(12.dp)) {
                    Text("Manage Adult", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SmartVoiceBottomBar(onHomeClick = navigateHome)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CompactInfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(color = PillGrey, shape = RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 9.dp)) {
        Column {
            Text(label, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor, letterSpacing = 0.4.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(value.ifEmpty { "—" }, fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor)
        }
    }
}

@Composable
private fun CompactPasswordTile(password: String, isVisible: Boolean, onToggle: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp)).padding(start = 12.dp, end = 4.dp, top = 9.dp, bottom = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Password", fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(1.dp))
                Text(if (isVisible) password else "••••••••", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TileTextColor)
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = if (isVisible) "Hide" else "Show", tint = PlaceholderColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun EditAdultDialog(database: SmartVoiceDatabase, originalAdult: User, isAccountHolder: Boolean, onDismiss: () -> Unit, onConfirm: (User) -> Unit) {
    var firstName by remember { mutableStateOf(originalAdult.firstName) }
    var lastName by remember { mutableStateOf(originalAdult.lastName) }
    var preferredName by remember { mutableStateOf(originalAdult.preferredName) }
    var username by remember { mutableStateOf(originalAdult.username) }
    var phoneDigits by remember { mutableStateOf(if (originalAdult.phone.startsWith("+44")) originalAdult.phone.substring(3) else originalAdult.phone) }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Manage Adult", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LogoBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                EditDialogField(value = firstName, label = "First Name", onValueChange = { firstName = filterNameInput(it) }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                EditDialogField(value = preferredName, label = "Preferred First Name", onValueChange = { preferredName = filterNameInput(it) }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                EditDialogField(value = lastName, label = "Last Name", onValueChange = { lastName = filterNameInput(it) }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                EditUkPhoneDialogField(digits = phoneDigits, onDigitsChange = { phoneDigits = it }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                if (isAccountHolder) {
                    EditDialogField(value = username, label = "Username", onValueChange = { username = it }, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                }
                LockedField(label = "Email", value = originalAdult.email)
                if (isAccountHolder) {
                    LockedField(label = "Password", value = "••••••••")
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(color = InfoBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("To change your password, sign out and use the reset option on the login screen.", fontFamily = InterFont, fontSize = 12.sp, color = InfoBlue, lineHeight = 17.sp)
                    }
                }
                if (error.isNotEmpty()) Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saving) return@Button
                    val firstTrim = firstName.trim()
                    val lastTrim = lastName.trim()
                    val phoneTrim = phoneDigits.trim()
                    val userTrim = username.trim()
                    when {
                        firstTrim.isBlank() -> error = "First name cannot be empty."
                        lastTrim.isBlank() -> error = "Last name cannot be empty."
                        phoneTrim.length !in 9..10 -> error = "Please enter a valid 9 or 10 digit mobile number."
                        isAccountHolder && userTrim.isBlank() -> error = "Username cannot be empty."
                        else -> {
                            saving = true; error = ""
                            scope.launch {
                                if (isAccountHolder && userTrim != originalAdult.username) {
                                    val taken = withContext(Dispatchers.IO) { database.userDao().checkIfUsernameExists(userTrim) > 0 }
                                    if (taken) { error = "That username is already taken."; saving = false; return@launch }
                                }
                                onConfirm(originalAdult.copy(firstName = firstTrim, lastName = lastTrim, preferredName = preferredName.trim(), username = if (isAccountHolder) userTrim else originalAdult.username, phone = "+44$phoneTrim"))
                                saving = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !saving
            ) { Text(if (saving) "Saving..." else "Save", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) } }
    )
}

@Composable
private fun UkPhoneDialogField(digits: String, isError: Boolean = false, keyboardActions: KeyboardActions = KeyboardActions.Default, onDigitsChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = if (isError) ErrorRed.copy(alpha = 0.05f) else PillGrey, shape = RoundedCornerShape(12.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(buildAnnotatedString { withStyle(SpanStyle(color = RequiredRed, fontWeight = FontWeight.Bold)) { append("* ") }; append("+44") }, fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TileTextColor, modifier = Modifier.padding(start = 16.dp))
            TextField(value = digits, onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(10)) },
                placeholder = { Text("Mobile Number", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun EditDialogField(value: String, label: String, keyboardActions: KeyboardActions = KeyboardActions.Default, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        TextField(value = value, onValueChange = onValueChange,
            placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
            textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EditUkPhoneDialogField(digits: String, keyboardActions: KeyboardActions = KeyboardActions.Default, onDigitsChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("+44", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TileTextColor, modifier = Modifier.padding(start = 16.dp))
            TextField(value = digits, onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(10)) },
                placeholder = { Text("Mobile Number", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun LockedField(label: String, value: String) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column {
            Text("$label (cannot be changed)", fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor.copy(alpha = 0.7f), letterSpacing = 0.3.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PlaceholderColor)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DeleteAccountConfirmationDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, isDeleting: Boolean, error: String?) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Delete Account", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ErrorRed) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().background(color = ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("WARNING: THIS ACTION CANNOT BE UNDONE", fontFamily = InterFont, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ErrorRed, letterSpacing = 0.5.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WarningItem("Your account holder profile")
                            WarningItem("All adult profiles linked to your account")
                            WarningItem("All child profiles and their information")
                            WarningItem("All saved voice samples")
                            WarningItem("All diagnoses and medical history")
                            WarningItem("All app settings and preferences")
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ENTER PASSWORD TO CONFIRM", fontFamily = InterFont, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TileTextColor, letterSpacing = 0.3.sp)
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        placeholder = { Text("Password", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null, tint = PlaceholderColor, modifier = Modifier.size(20.dp))
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true); keyboardController?.hide() }),
                        isError = error != null, enabled = !isDeleting,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ErrorRed, unfocusedBorderColor = Color(0xFFD1D5DB), focusedLabelColor = ErrorRed, errorBorderColor = ErrorRed)
                    )
                }
                if (error != null) Text(error, color = ErrorRed, fontSize = 12.sp, fontFamily = InterFont, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = { focusManager.clearFocus(force = true); keyboardController?.hide(); onConfirm(password) },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = White),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDeleting && password.isNotBlank()
            ) { Text(if (isDeleting) "Deleting..." else "Permanently Delete Account", fontFamily = InterFont, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = { focusManager.clearFocus(force = true); keyboardController?.hide(); onDismiss() }, enabled = !isDeleting) {
                Text("Cancel", fontFamily = InterFont, color = LogoBlue)
            }
        }
    )
}

@Composable
private fun WarningItem(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("*", fontFamily = InterFont, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed, modifier = Modifier.padding(top = 1.dp))
        Text(text, fontFamily = InterFont, fontSize = 14.sp, color = ErrorRed, lineHeight = 18.sp)
    }
}