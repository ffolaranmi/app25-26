package com.example.smartvoice.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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


object AddAdultDestination : NavigationDestination {
    override val route = "addAdult"; override val titleRes = R.string.app_name
}
object AccountInfoDestination : NavigationDestination {
    override val route = "accountInfo"; override val titleRes = R.string.app_name
}
object ManageAdultsDestination : NavigationDestination {
    override val route = "manageAdults"; override val titleRes = R.string.app_name
}
object ViewChildInfoDestination : NavigationDestination {
    override val route = "viewChildInfo"; override val titleRes = R.string.app_name
}
object AdultsDestination : NavigationDestination {
    override val route = "accountInfo"; override val titleRes = R.string.app_name
}
object AdultDetailDestination : NavigationDestination {
    override val route = "adultDetail"; override val titleRes = R.string.app_name
    const val adultIdArg = "adultId"
    val routeWithArgs = "$route/{$adultIdArg}"
}

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)
private val TileTextColor    = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)
private val InfoBlue         = Color(0xFF1565C0)

class AdultsViewModel(private val db: SmartVoiceDatabase) : ViewModel() {
    private val _adults = MutableStateFlow<List<User>>(emptyList())
    val adults: StateFlow<List<User>> = _adults

    private val _selectedAdult = MutableStateFlow<User?>(null)
    val selectedAdult: StateFlow<User?> = _selectedAdult

    private val _accountHolderId = MutableStateFlow<Long?>(null)
    val accountHolderId: StateFlow<Long?> = _accountHolderId

    fun setAccountHolderId(id: Long?) { _accountHolderId.value = id }

    fun loadAdults(userId: Long) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getUserById(userId) }
            _adults.value = if (user != null) listOf(user) else emptyList()
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
            _adults.value = listOf(user)
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
    val userId  = remember { SessionPrefs.getLoggedInUserId(context) }

    val accountVm: AccountInfoViewModel = viewModel(factory = AccountInfoViewModelFactory(database, context))
    val accountHolder by accountVm.user.collectAsState()
    val isDeleting    by accountVm.isDeleting.collectAsState()
    val deleteError   by accountVm.deleteError.collectAsState()

    val vm: AdultsViewModel = viewModel(factory = AdultsViewModelFactory(database))
    val adults          by vm.adults.collectAsState()
    val accountHolderId by vm.accountHolderId.collectAsState()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != -1L) { vm.setAccountHolderId(userId); vm.loadAdults(userId) }
    }

    if (showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = { password ->
                accountVm.deleteAccountWithPassword(
                    password  = password,
                    onSuccess = { showDeleteAccountDialog = false; navigateToLogin() },
                    onError   = { }
                )
            },
            isDeleting = isDeleting,
            error = deleteError
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = navigateToHome) {
                        Icon(Icons.Filled.Home, contentDescription = "Home", tint = LogoBlue, modifier = Modifier.size(48.dp))
                    }
                    Text(
                        text = "Adult Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (adults.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrightBlue)
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 20.dp)
                        ) {
                            items(adults) { adult ->
                                val isHolder = accountHolderId != null && adult.id == accountHolderId
                                AdultListTile(
                                    adult = adult,
                                    isAccountHolder = isHolder,
                                    onClick = { navController.navigate("${AdultDetailDestination.route}/${adult.id}") },
                                    onDeleteClick = {}
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navigateToScreenOption(ChildInfoDestination) },
                    modifier = Modifier.width(180.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Child Info", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = LogoBlue)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.width(200.dp).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Delete Account", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.weight(0.05f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-1.5).sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = adult.preferredName.ifBlank { "${adult.firstName} ${adult.lastName}".trim() }.ifEmpty { adult.username },
                    fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TileTextColor
                )
                if (isAccountHolder) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(color = BrightBlue.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ACCOUNT HOLDER", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = BrightBlue, letterSpacing = 0.6.sp)
                    }
                }
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = LogoBlue.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun AdultDetailScreen(
    adultId: Long,
    database: SmartVoiceDatabase,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    navigateToAllAdults: () -> Unit,
) {
    val context = LocalContext.current
    val accountVm: AccountInfoViewModel = viewModel(factory = AccountInfoViewModelFactory(database, context))
    val accountHolder by accountVm.user.collectAsState()

    val vm: AdultsViewModel = viewModel(factory = AdultsViewModelFactory(database))
    val adult by vm.selectedAdult.collectAsState()

    val isAccountHolder  = adult != null && adult?.id == accountHolder?.id
    var showManageDialog by remember { mutableStateOf(false) }
    var showPassword     by remember { mutableStateOf(false) }

    LaunchedEffect(adultId) { vm.loadAdultById(adultId) }

    if (showManageDialog && adult != null) {
        EditAdultDialog(
            database        = database,
            originalAdult   = adult!!,
            isAccountHolder = isAccountHolder,
            onDismiss       = { showManageDialog = false },
            onConfirm       = { updated -> vm.updateAdult(updated); showManageDialog = false }
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = navigateHome) {
                        Icon(Icons.Filled.Home, contentDescription = "Home", tint = LogoBlue, modifier = Modifier.size(48.dp))
                    }
                    Text(
                        text = adult?.let { it.preferredName.ifBlank { "${it.firstName} ${it.lastName}".trim() } } ?: "Adult",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-1.5).sp,
                        color = LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                adult?.let { a ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactInfoTile(label = "First Name", value = a.firstName, modifier = Modifier.weight(1f))
                        CompactInfoTile(label = "Last Name",  value = a.lastName,  modifier = Modifier.weight(1f))
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
                        Box(
                            modifier = Modifier
                                .background(color = BrightBlue.copy(alpha = 0.10f), shape = RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("ACCOUNT HOLDER", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BrightBlue, letterSpacing = 0.6.sp)
                        }
                    }

                } ?: Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrightBlue)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { showManageDialog = true },
                    modifier = Modifier.width(220.dp).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manage Adult", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = navigateToAllAdults,
                    modifier = Modifier.width(220.dp).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("All Adults", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = LogoBlue)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-1.5).sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactInfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = PillGrey, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column {
            Text(text = label, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor, letterSpacing = 0.4.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value.ifEmpty { "—" },
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor
            )
        }
    }
}

@Composable
private fun CompactPasswordTile(password: String, isVisible: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PillGrey, shape = RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 4.dp, top = 9.dp, bottom = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Password", fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = if (isVisible) password else "••••••••",
                    fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TileTextColor
                )
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Hide" else "Show",
                    tint = PlaceholderColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EditAdultDialog(
    database: SmartVoiceDatabase,
    originalAdult: User,
    isAccountHolder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var firstName     by remember { mutableStateOf(originalAdult.firstName) }
    var lastName      by remember { mutableStateOf(originalAdult.lastName) }
    var preferredName by remember { mutableStateOf(originalAdult.preferredName) }
    var username      by remember { mutableStateOf(originalAdult.username) }
    var phoneDigits   by remember {
        mutableStateOf(if (originalAdult.phone.startsWith("+44")) originalAdult.phone.substring(3) else originalAdult.phone)
    }
    var error  by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope  = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Manage Adult", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LogoBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                DialogField(firstName, "First Name") { firstName = it }
                DialogField(preferredName, "Preferred First Name (optional)") { preferredName = it }
                DialogField(lastName, "Last Name") { lastName = it }
                UkPhoneDialogField(digits = phoneDigits, onDigitsChange = { phoneDigits = it })
                if (isAccountHolder) { DialogField(username, "Username") { username = it } }
                LockedField(label = "Email", value = originalAdult.email)
                if (isAccountHolder) {
                    LockedField(label = "Password", value = "••••••••")
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = InfoBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
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
                    val firstTrim = firstName.trim(); val lastTrim = lastName.trim()
                    val phoneTrim = phoneDigits.trim(); val userTrim = username.trim()
                    when {
                        firstTrim.isBlank() -> error = "First name cannot be empty."
                        lastTrim.isBlank()  -> error = "Last name cannot be empty."
                        phoneTrim.length != 9 -> error = "Please enter a valid 9-digit mobile number."
                        isAccountHolder && userTrim.isBlank() -> error = "Username cannot be empty."
                        else -> {
                            saving = true; error = ""
                            scope.launch {
                                if (isAccountHolder && userTrim != originalAdult.username) {
                                    val taken = withContext(Dispatchers.IO) { database.userDao().checkIfUsernameExists(userTrim) > 0 }
                                    if (taken) { error = "That username is already taken."; saving = false; return@launch }
                                }
                                onConfirm(originalAdult.copy(
                                    firstName     = firstTrim,
                                    lastName      = lastTrim,
                                    preferredName = preferredName.trim(),
                                    username      = if (isAccountHolder) userTrim else originalAdult.username,
                                    phone         = "+44$phoneTrim"
                                ))
                                saving = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !saving
            ) {
                Text(if (saving) "Saving..." else "Save", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) }
        }
    )
}

@Composable
private fun UkPhoneDialogField(digits: String, onDigitsChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("+44", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TileTextColor, modifier = Modifier.padding(start = 16.dp))
            TextField(
                value = digits,
                onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(9)) },
                placeholder = { Text("Mobile Number", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor
                ),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DialogField(value: String, label: String, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor
            ),
            textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LockedField(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PillGrey.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text("$label (cannot be changed)", fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = PlaceholderColor.copy(alpha = 0.7f), letterSpacing = 0.3.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PlaceholderColor)
        }
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, isDeleting: Boolean, error: String?) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Delete Account", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ErrorRed) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(color = ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "WARNING: THIS ACTION CANNOT BE UNDONE",
                            fontFamily = InterFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ErrorRed,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "The following will be deleted:",
                            fontFamily = InterFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WarningItem("Your account holder profile")
                            WarningItem("All child profiles and their information")
                            WarningItem("All saved voice samples")
                            WarningItem("All diagnoses and medical history")
                            WarningItem("All app settings and preferences")
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ENTER PASSWORD TO CONFIRM",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TileTextColor,
                        letterSpacing = 0.3.sp
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null, tint = PlaceholderColor, modifier = Modifier.size(20.dp))
                            }
                        },
                        isError = error != null,
                        enabled = !isDeleting,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = Color.Transparent,
                            focusedLabelColor = ErrorRed,
                            errorBorderColor = ErrorRed
                        )
                    )
                }
                if (error != null) {
                    Text(error, color = ErrorRed, fontSize = 12.sp, fontFamily = InterFont, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = White),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDeleting && password.isNotBlank()
            ) {
                Text(if (isDeleting) "Deleting..." else "Permanently Delete Account", fontFamily = InterFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) }
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