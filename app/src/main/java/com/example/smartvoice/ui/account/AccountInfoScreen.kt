package com.example.smartvoice.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartvoice.R
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LightBlue
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import kotlinx.coroutines.launch

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

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)
private val TileTextColor   = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)

private fun isPasswordStrong(password: String): Boolean {
    val hasNumber = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    return password.length >= 8 && hasNumber && hasSymbol
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
    val viewModel: AccountInfoViewModel = viewModel(
        factory = AccountInfoViewModelFactory(database, context)
    )
    val user by viewModel.user.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showResetDialog        by remember { mutableStateOf(false) }
    var showDeleteDialog       by remember { mutableStateOf(false) }
    var resetEmail             by remember { mutableStateOf("") }
    var currentPassword        by remember { mutableStateOf("") }
    var newPassword            by remember { mutableStateOf("") }
    var resetErrorMessage      by remember { mutableStateOf("") }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var deleteError            by remember { mutableStateOf("") }
    var showCurrentPassword    by remember { mutableStateOf(false) }
    var showNewPassword        by remember { mutableStateOf(false) }
    var showTilePassword       by remember { mutableStateOf(false) }

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
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navigateToHome() }) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "Account Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val displayUsername = user?.username ?: "username"
                Text(
                    text = displayUsername,
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 22.sp,
                    color = BrightBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                val u = user
                AccountTile(label = "First Name",   value = u?.username ?: "")
                AccountTile(label = "Last Name",     value = "")
                AccountTile(label = "Mobile Number", value = u?.phone   ?: "")
                AccountTile(label = "Email",         value = u?.email   ?: "")
                PasswordTile(
                    value = u?.password ?: "",
                    isVisible = showTilePassword,
                    onToggle = { showTilePassword = !showTilePassword }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navigateToScreenOption(AddAdultDestination) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "+ Add Adult",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LogoBlue
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = { navigateToScreenOption(ManageAdultsDestination) }) {
                    Text(
                        text = "Manage Adults",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrightBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { navigateToScreenOption(ViewChildInfoDestination) },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "View Child Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
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

                /*
                Button(
                    onClick = { resetEmail = u?.email ?: ""; showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.fillMaxWidth().height(70.dp)
                ) {
                    Text("Reset Password", color = White, fontSize = 18.sp)
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.fillMaxWidth().height(70.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Account", tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", color = White, fontSize = 18.sp)
                }
                */
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showResetDialog = false
                        resetEmail = ""; currentPassword = ""; newPassword = ""; resetErrorMessage = ""
                    },
                    title = { Text("Reset Password") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = resetEmail, onValueChange = { resetEmail = it },
                                label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it },
                                label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(if (showCurrentPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                                    }
                                })
                            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it },
                                label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(if (showNewPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                                    }
                                })
                            if (resetErrorMessage.isNotEmpty()) Text(resetErrorMessage, color = ErrorRed)
                        }
                    },
                    confirmButton = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                coroutineScope.launch {
                                    if (resetEmail.isBlank() || currentPassword.isBlank() || newPassword.isBlank()) {
                                        resetErrorMessage = "Please fill in all fields."; return@launch
                                    }
                                    if (!isPasswordStrong(newPassword)) {
                                        resetErrorMessage = "Password not strong enough."; return@launch
                                    }
                                    val ok = viewModel.resetPassword(resetEmail.trim(), currentPassword, newPassword)
                                    if (ok) { showResetDialog = false; navigateToLogin() }
                                    else resetErrorMessage = "Incorrect email/password or error updating."
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                                Text("Reset", color = White)
                            }
                        }
                    },
                    dismissButton = {}
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; deleteConfirmationText = ""; deleteError = "" },
                    title = { Text("Confirm Account Deletion") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Type 'DELETE' to confirm account deletion.")
                            OutlinedTextField(value = deleteConfirmationText, onValueChange = { deleteConfirmationText = it },
                                label = { Text("Enter DELETE") }, modifier = Modifier.fillMaxWidth())
                            if (deleteError.isNotEmpty()) Text(deleteError, color = ErrorRed)
                        }
                    },
                    confirmButton = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showDeleteDialog = false; deleteConfirmationText = ""; deleteError = "" }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                coroutineScope.launch {
                                    if (deleteConfirmationText == "DELETE") {
                                        val deleted = viewModel.deleteAccount()
                                        if (deleted) { showDeleteDialog = false; navigateToLogin() }
                                        else deleteError = "Could not delete account."
                                    } else deleteError = "You must type DELETE to confirm."
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                                Text("Confirm Delete", color = White)
                            }
                        }
                    },
                    dismissButton = {}
                )
            }
        }
    }
}

@Composable
fun AccountTile(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = if (value.isNotEmpty()) value else label,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor
        )
    }
}

@Composable
fun PasswordTile(
    value: String,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isVisible) value else "••••••••",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Hide password" else "Show password",
                    tint = Color(0xFF374151)
                )
            }
        }
    }
}

@Composable
fun InfoTile(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary)
            Text(text = value, fontSize = 16.sp)
        }
    }
}