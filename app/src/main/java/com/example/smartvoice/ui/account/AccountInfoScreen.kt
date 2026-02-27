package com.example.smartvoice.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    override val route = "addAdult"
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
object AdultsDestination : NavigationDestination {
    override val route = "accountInfo"
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

    fun loadAdults() {
        viewModelScope.launch {
            _adults.value = withContext(Dispatchers.IO) {
                db.userDao().getAllUsersNewestFirst()
            }
        }
    }

    fun loadAdultById(id: Long) {
        viewModelScope.launch {
            _selectedAdult.value = withContext(Dispatchers.IO) {
                db.userDao().getUserById(id)
            }
        }
    }

    fun addAdult(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().insert(user) }
            loadAdults()
        }
    }

    fun updateAdult(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().update(user) }
            loadAdults()
            _selectedAdult.value = user
        }
    }

    fun deleteAdult(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.userDao().delete(user) }
            loadAdults()
        }
    }
}

class AdultsViewModelFactory(private val db: SmartVoiceDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdultsViewModel(db) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    database: SmartVoiceDatabase,
    navController: NavController,
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToScreenOption: (NavigationDestination) -> Unit
) {
    val context = LocalContext.current

    val accountVm: AccountInfoViewModel = viewModel(
        factory = AccountInfoViewModelFactory(database, context)
    )
    val accountHolder by accountVm.user.collectAsState()
    val isDeleting by accountVm.isDeleting.collectAsState()
    val deleteError by accountVm.deleteError.collectAsState()

    val vm: AdultsViewModel = viewModel(factory = AdultsViewModelFactory(database))
    val adults          by vm.adults.collectAsState()
    val accountHolderId by vm.accountHolderId.collectAsState()

    var showAddDialog    by remember { mutableStateOf(false) }
    var showDeleteAdultDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(accountHolder?.id) {
        vm.setAccountHolderId(accountHolder?.id)
        vm.loadAdults()
    }

    showDeleteAdultDialog?.let { target ->
        AlertDialog(
            onDismissRequest = { showDeleteAdultDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Remove Adult?", fontFamily = InterFont, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Are you sure you want to remove ${target.firstName} ${target.lastName}?",
                        fontFamily = InterFont
                    )
                    Text(
                        "This action cannot be undone.",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { vm.deleteAdult(target); showDeleteAdultDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontFamily = InterFont, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAdultDialog = null }) {
                    Text("Cancel", fontFamily = InterFont, color = LogoBlue)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onDismiss = {
                showDeleteAccountDialog = false
            },
            onConfirm = { password ->
                accountVm.deleteAccountWithPassword(
                    password = password,
                    onSuccess = {
                        showDeleteAccountDialog = false
                        navigateToLogin()
                    },
                    onError = { }
                )
            },
            isDeleting = isDeleting,
            error = deleteError
        )
    }

    if (showAddDialog) {
        AddAdultDialog(
            database = database,
            onDismiss = { showAddDialog = false },
            onConfirm = { newUser ->
                vm.addAdult(newUser)
                showAddDialog = false
            }
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = navigateToHome) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
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
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No adults added yet.\nTap '+ Add Adult' to get started.",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = PlaceholderColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(adults) { adult ->
                            val isHolder = accountHolderId != null && adult.id == accountHolderId
                            AdultListTile(
                                adult = adult,
                                isAccountHolder = isHolder,
                                onClick = {
                                    navController.navigate("${AdultDetailDestination.route}/${adult.id}")
                                },
                                onDeleteClick = {
                                    if (!isHolder) {
                                        showDeleteAdultDialog = adult
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.width(180.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+ Add Adult",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { navigateToScreenOption(ChildInfoDestination) },
                    modifier = Modifier.width(180.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Child Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LogoBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier
                        .width(200.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Delete Account",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.weight(0.05f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun AdultListTile(
    adult: User,
    isAccountHolder: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = PillGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${adult.firstName} ${adult.lastName}".trim().ifEmpty { adult.username },
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = TileTextColor
                )
                if (isAccountHolder) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = BrightBlue.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACCOUNT HOLDER",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = BrightBlue,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LogoBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
                if (!isAccountHolder) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove adult",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }
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
    val accountVm: AccountInfoViewModel = viewModel(
        factory = AccountInfoViewModelFactory(database, context)
    )
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
            onConfirm       = { updated ->
                vm.updateAdult(updated)
                showManageDialog = false
            }
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = navigateHome) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = adult?.let { "${it.firstName} ${it.lastName}".trim() } ?: "Adult",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-1.5).sp,
                        color = LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                adult?.let { a ->
                    InfoTileRow(label = "First Name",    value = a.firstName)
                    InfoTileRow(label = "Last Name",     value = a.lastName)
                    InfoTileRow(label = "Mobile Number", value = a.phone)
                    InfoTileRow(label = "Email",         value = a.email)

                    if (isAccountHolder) {
                        InfoTileRow(label = "Username", value = a.username)
                        PasswordTileRow(
                            password  = a.password,
                            isVisible = showPassword,
                            onToggle  = { showPassword = !showPassword }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = BrightBlue.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ACCOUNT HOLDER",
                                fontFamily = InterFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = BrightBlue,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                } ?: run {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrightBlue)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = { showManageDialog = true },
                    modifier = Modifier.width(220.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Manage Adult",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = navigateToAllAdults,
                    modifier = Modifier.width(220.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "All Adults",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LogoBlue
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoTileRow(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                text = label,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PlaceholderColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "—" },
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor
            )
        }
    }
}

@Composable
private fun PasswordTileRow(
    password: String,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Password",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = PlaceholderColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isVisible) password else "••••••••",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TileTextColor
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Hide" else "Show",
                    tint = PlaceholderColor
                )
            }
        }
    }
}

@Composable
private fun AddAdultDialog(
    database: SmartVoiceDatabase,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf("") }
    var checking  by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(dismissOnClickOutside = false),
        title = {
            Text(
                "Add Adult",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = LogoBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(firstName, "First Name")    { firstName = it }
                DialogField(lastName,  "Last Name")     { lastName = it }
                DialogField(email,     "Email")         { email = it }

                UkPhoneDialogField(
                    digits = phoneDigits,
                    onDigitsChange = { phoneDigits = it }
                )

                if (error.isNotEmpty()) {
                    Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (checking) return@Button
                    val emailTrim = email.trim()
                    val fullPhone = "+44$phoneDigits"

                    when {
                        firstName.isBlank() || lastName.isBlank() ->
                            error = "Please enter a first and last name."
                        emailTrim.isBlank() || !emailTrim.contains("@") ->
                            error = "Please enter a valid email."
                        phoneDigits.length != 9 ->
                            error = "Please enter a valid 9-digit mobile number."
                        else -> {
                            checking = true; error = ""
                            scope.launch {
                                val exists = withContext(Dispatchers.IO) {
                                    database.userDao().checkIfEmailExists(emailTrim) > 0
                                }
                                if (exists) {
                                    error = "That email is already in use."
                                    checking = false
                                    return@launch
                                }
                                onConfirm(
                                    User(
                                        firstName = firstName.trim(),
                                        lastName  = lastName.trim(),
                                        username  = "${firstName.trim()} ${lastName.trim()}",
                                        email     = emailTrim,
                                        phone     = fullPhone,
                                        password  = ""
                                    )
                                )
                                checking = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !checking
            ) {
                Text(
                    if (checking) "Adding..." else "Add",
                    fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = InterFont, color = LogoBlue)
            }
        }
    )
}

@Composable
private fun UkPhoneDialogField(
    digits: String,
    onDigitsChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PillGrey, shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "+44",
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TileTextColor,
                modifier = Modifier.padding(start = 16.dp)
            )
            TextField(
                value = digits,
                onValueChange = { raw -> onDigitsChange(raw.filter { it.isDigit() }.take(9)) },
                placeholder = {
                    Text("Mobile Number", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = TileTextColor,
                    unfocusedTextColor      = TileTextColor
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 15.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
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
    var firstName by remember { mutableStateOf(originalAdult.firstName) }
    var lastName  by remember { mutableStateOf(originalAdult.lastName) }
    var username  by remember { mutableStateOf(originalAdult.username) }
    var phoneDigits by remember {
        mutableStateOf(
            if (originalAdult.phone.startsWith("+44"))
                originalAdult.phone.substring(3)
            else
                originalAdult.phone
        )
    }
    var error     by remember { mutableStateOf("") }
    var saving    by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Manage Adult",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = LogoBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                DialogField(firstName, "First Name")    { firstName = it }
                DialogField(lastName,  "Last Name")     { lastName = it }

                UkPhoneDialogField(
                    digits = phoneDigits,
                    onDigitsChange = { phoneDigits = it }
                )

                if (isAccountHolder) {
                    DialogField(username, "Username") { username = it }
                }

                LockedField(label = "Email", value = originalAdult.email)

                if (isAccountHolder) {
                    LockedField(label = "Password", value = "••••••••")
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = InfoBlue.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "To change your password, sign out and use the reset option on the login screen.",
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            color = InfoBlue,
                            lineHeight = 17.sp
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saving) return@Button
                    val firstTrim = firstName.trim()
                    val lastTrim  = lastName.trim()
                    val phoneTrim = phoneDigits.trim()
                    val userTrim  = username.trim()
                    val fullPhone = "+44$phoneTrim"

                    when {
                        firstTrim.isBlank() -> error = "First name cannot be empty."
                        lastTrim.isBlank()  -> error = "Last name cannot be empty."
                        phoneTrim.length != 9 -> error = "Please enter a valid 9-digit mobile number."
                        isAccountHolder && userTrim.isBlank() ->
                            error = "Username cannot be empty."
                        else -> {
                            saving = true; error = ""
                            scope.launch {
                                if (isAccountHolder && userTrim != originalAdult.username) {
                                    val taken = withContext(Dispatchers.IO) {
                                        database.userDao().checkIfUsernameExists(userTrim) > 0
                                    }
                                    if (taken) {
                                        error = "That username is already taken."
                                        saving = false
                                        return@launch
                                    }
                                }
                                onConfirm(
                                    originalAdult.copy(
                                        firstName = firstTrim,
                                        lastName  = lastTrim,
                                        username  = if (isAccountHolder) userTrim else originalAdult.username,
                                        phone     = fullPhone
                                    )
                                )
                                saving = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !saving
            ) {
                Text(
                    if (saving) "Saving..." else "Save",
                    fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = InterFont, color = LogoBlue)
            }
        }
    )
}

@Composable
private fun DialogField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PillGrey, shape = RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp)
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor        = TileTextColor,
                unfocusedTextColor      = TileTextColor
            ),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LockedField(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = PillGrey.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = "$label (cannot be changed)",
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = PlaceholderColor.copy(alpha = 0.7f),
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PlaceholderColor
            )
        }
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDeleting: Boolean,
    error: String?
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Delete Account",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = ErrorRed
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = ErrorRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "WARNING: This action cannot be undone!\n\n" +
                                "Deleting your account will permanently remove:\n" +
                                "• Your account holder profile\n" +
                                "• All other adult profiles\n" +
                                "• All child profiles and their information\n" +
                                "• All saved voice samples\n" +
                                "• All diagnoses and medical history\n" +
                                "• All app settings and preferences",
                        fontFamily = InterFont,
                        fontSize = 14.sp,
                        color = ErrorRed,
                        lineHeight = 20.sp
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter your password to confirm") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = error != null,
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErrorRed,
                        focusedLabelColor = ErrorRed
                    )
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontFamily = InterFont,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = White
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDeleting && password.isNotBlank()
            ) {
                Text(
                    if (isDeleting) "Deleting..." else "Permanently Delete Account",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel", fontFamily = InterFont, color = LogoBlue)
            }
        }
    )
}