package com.example.smartvoice.ui.child

import com.example.smartvoice.ui.child.ChildViewModel
import com.example.smartvoice.ui.child.ChildViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.R
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.account.AccountInfoViewModel
import com.example.smartvoice.ui.account.AccountInfoViewModelFactory
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.*
import java.util.Calendar


object ChildInfoDestination : NavigationDestination {
    override val route = "childInfo"
    override val titleRes = R.string.app_name
}

object ChildDetailDestination : NavigationDestination {
    override val route = "childDetail"
    override val titleRes = R.string.app_name
    const val childIdArg = "childId"
    val routeWithArgs = "$route/{$childIdArg}"
}


private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun calculateAge(birthMonth: Int, birthYear: Int): String {
    val now     = Calendar.getInstance()
    val curYear = now.get(Calendar.YEAR)
    val curMonth = now.get(Calendar.MONTH) + 1
    var age = curYear - birthYear
    if (curMonth < birthMonth) age--
    return if (age < 0) "Unknown" else "$age years old"
}

private val genderOptions = listOf("Female", "Male")

@Composable
fun ChildInfoScreen(
    database: SmartVoiceDatabase,
    navigateBack: () -> Unit,
    navigateToChildDetail: (Long) -> Unit,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val accountVm: AccountInfoViewModel = viewModel(
        factory = AccountInfoViewModelFactory(database, context)
    )
    val accountHolder by accountVm.user.collectAsState()

    val viewModel: ChildViewModel = viewModel(factory = ChildViewModelFactory(database))
    val children by viewModel.children.collectAsState()

    var showDeleteDialog  by remember { mutableStateOf<ChildTable?>(null) }
    var showAddDialog     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadChildren() }

    showDeleteDialog?.let { child ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Remove Child?", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = if (isDark) White else Color.Unspecified)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Are you sure you want to remove ${child.firstName} ${child.lastName}?",
                        fontFamily = InterFont,
                        color = if (isDark) DarkTextPrimary else Color.Unspecified
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
                    onClick = { viewModel.deleteChild(child); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontFamily = InterFont, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", fontFamily = InterFont, color = if (isDark) LightBlue else LogoBlue)
                }
            }
        )
    }

    if (showAddDialog) {
        AddChildDialog(
            accountHolderId = accountHolder?.id ?: 0L,
            onDismiss = { showAddDialog = false },
            onConfirm = { child ->
                viewModel.addChild(child)
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
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = if (isDark) White else LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "Child Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = if (isDark) White else LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (children.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No children added yet.\nTap '+ Add Child' to get started.",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = if (isDark) DarkTextSecondary else Color(0xFF4B5563),
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
                        items(children) { child ->
                            ChildListTile(
                                child = child,
                                onClick = { navigateToChildDetail(child.id) },
                                onDeleteClick = { showDeleteDialog = child }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+ Add Child",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.weight(0.05f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (isDark) White else LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ChildListTile(
    child: ChildTable,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkPill else PillGrey)
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
                    text = "${child.firstName} ${child.lastName}",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = if (isDark) White else Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = calculateAge(child.birthMonth, child.birthYear),
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isDark) DarkTextSecondary else Color(0xFF4B5563)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isDark) White.copy(alpha = 0.5f) else LogoBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove child",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChildDetailScreen(
    childId: Long,
    database: SmartVoiceDatabase,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    navigateToAllChildren: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val viewModel: ChildViewModel = viewModel(factory = ChildViewModelFactory(database))
    val child by viewModel.selectedChild.collectAsState()

    var showManageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(childId) { viewModel.loadChildById(childId) }

    if (showManageDialog) {
        child?.let { c ->
            EditChildDialog(
                child = c,
                onDismiss = { showManageDialog = false },
                onConfirm = { updated ->
                    viewModel.updateChild(updated)
                    showManageDialog = false
                }
            )
        }
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
                            tint = if (isDark) White else LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = child?.let { "${it.firstName} ${it.lastName}" } ?: "Child",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = if (isDark) White else LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                child?.let { c ->
                    InfoTileRow(label = "First Name",  value = c.firstName)
                    InfoTileRow(label = "Last Name",   value = c.lastName)
                    InfoTileRow(label = "Gender",      value = c.gender)
                    InfoTileRow(
                        label = "Birth Month & Year",
                        value = "${monthNames.getOrNull(c.birthMonth - 1) ?: c.birthMonth} ${c.birthYear}"
                    )
                    InfoTileRow(
                        label = "Age",
                        value = calculateAge(c.birthMonth, c.birthYear)
                    )
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
                        text = "Manage Child Info",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = navigateToAllChildren,
                    modifier = Modifier.width(220.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkPill else White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LightBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "All Children",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (isDark) White else LogoBlue
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (isDark) White else LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoTileRow(label: String, value: String) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = if (isDark) DarkPill else PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                text = label,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isDark) DarkTextSecondary else Color(0xFF4B5563),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "—" },
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (value.isNotEmpty()) (if (isDark) White else Color(0xFF111827)) else (if (isDark) DarkTextSecondary else Color(0xFF4B5563))
            )
        }
    }
}

@Composable
private fun AddChildDialog(
    accountHolderId: Long,
    onDismiss: () -> Unit,
    onConfirm: (ChildTable) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf<String?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var birthYear by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(dismissOnClickOutside = false),
        title = {
            Text(
                "Add Child",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = if (isDark) White else LogoBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(value = firstName, label = "First Name", onValueChange = { firstName = it })
                DialogField(value = lastName,  label = "Last Name",  onValueChange = { lastName = it })

                SimpleDropdownField(
                    label = "Gender",
                    options = genderOptions,
                    selected = selectedGender,
                    onSelected = { selectedGender = it }
                )

                MonthDropdownField(
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it },
                    label = "Birth Month"
                )

                DialogField(value = birthYear, label = "Birth Year", onValueChange = { birthYear = it })

                if (error.isNotEmpty()) {
                    Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = birthYear.toIntOrNull()
                    val month = selectedMonth
                    val gender = selectedGender

                    val now = Calendar.getInstance()
                    val curYear = now.get(Calendar.YEAR)
                    val curMonth = now.get(Calendar.MONTH) + 1
                    val minAllowedYear = curYear - 18

                    when {
                        firstName.isBlank() || lastName.isBlank() ->
                            error = "Please enter a first and last name."
                        gender.isNullOrBlank() ->
                            error = "Please select a gender."
                        month == null ->
                            error = "Please select a birth month."
                        year == null || year < 1900 ->
                            error = "Please enter a valid birth year."

                        year > curYear || (year == curYear && month > curMonth) ->
                            error = "Birth month and year cannot be in the future."

                        year < minAllowedYear || (year == minAllowedYear && month < curMonth) ->
                            error = "This app is for children under 18."

                        else -> onConfirm(
                            ChildTable(
                                userId = accountHolderId,
                                firstName = firstName.trim(),
                                lastName  = lastName.trim(),
                                gender    = gender,
                                birthMonth = month,
                                birthYear  = year
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = InterFont, color = if (isDark) LightBlue else LogoBlue)
            }
        }
    )
}

@Composable
private fun EditChildDialog(
    child: ChildTable,
    onDismiss: () -> Unit,
    onConfirm: (ChildTable) -> Unit
) {
    var firstName by remember { mutableStateOf(child.firstName) }
    var lastName  by remember { mutableStateOf(child.lastName) }

    var selectedGender by remember { mutableStateOf(child.gender.takeIf { it.isNotBlank() }) }
    var selectedMonth by remember { mutableStateOf(child.birthMonth) }
    var birthYear by remember { mutableStateOf(child.birthYear.toString()) }
    var error by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Edit Child Info",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = if (isDark) White else LogoBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(value = firstName, label = "First Name", onValueChange = { firstName = it })
                DialogField(value = lastName,  label = "Last Name",  onValueChange = { lastName = it })

                SimpleDropdownField(
                    label = "Gender",
                    options = genderOptions,
                    selected = selectedGender,
                    onSelected = { selectedGender = it }
                )

                MonthDropdownField(
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it },
                    label = "Birth Month"
                )

                DialogField(value = birthYear, label = "Birth Year", onValueChange = { birthYear = it })

                if (error.isNotEmpty()) {
                    Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = birthYear.toIntOrNull()
                    val gender = selectedGender

                    val now = Calendar.getInstance()
                    val curYear = now.get(Calendar.YEAR)
                    val curMonth = now.get(Calendar.MONTH) + 1
                    val minAllowedYear = curYear - 18

                    when {
                        firstName.isBlank() || lastName.isBlank() ->
                            error = "Please enter a first and last name."
                        gender.isNullOrBlank() ->
                            error = "Please select a gender."
                        selectedMonth !in 1..12 ->
                            error = "Please select a birth month."
                        year == null || year < 1900 ->
                            error = "Please enter a valid birth year."

                        year > curYear || (year == curYear && selectedMonth > curMonth) ->
                            error = "Birth month and year cannot be in the future."

                        year < minAllowedYear || (year == minAllowedYear && selectedMonth < curMonth) ->
                            error = "This app is for children under 18."

                        else -> onConfirm(
                            child.copy(
                                firstName = firstName.trim(),
                                lastName  = lastName.trim(),
                                gender    = gender,
                                birthMonth = selectedMonth,
                                birthYear  = year

                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = InterFont, color = if (isDark) LightBlue else LogoBlue)
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
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (isDark) DarkField else PillGrey, shape = RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(label, fontFamily = InterFont, color = if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563), fontSize = 14.sp)
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor        = if (isDark) White else Color(0xFF111827),
                unfocusedTextColor      = if (isDark) White else Color(0xFF111827)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdownField(
    selectedMonth: Int?,
    onMonthSelected: (Int) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val displayText = selectedMonth?.let { monthNames.getOrNull(it - 1) } ?: label

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (isDark) DarkField else PillGrey, shape = RoundedCornerShape(12.dp))
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(label, fontFamily = InterFont, color = if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563), fontSize = 14.sp)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = if (selectedMonth != null) (if (isDark) White else Color(0xFF111827)) else (if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563)),
                    unfocusedTextColor      = if (selectedMonth != null) (if (isDark) White else Color(0xFF111827)) else (if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563))
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 15.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                monthNames.forEachIndexed { index, month ->
                    DropdownMenuItem(
                        text = { Text(month, fontFamily = InterFont) },
                        onClick = {
                            onMonthSelected(index + 1)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownField(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val displayText = selected ?: label

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (isDark) DarkField else PillGrey, shape = RoundedCornerShape(12.dp))
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(label, fontFamily = InterFont, color = if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563), fontSize = 14.sp)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = if (selected != null) (if (isDark) White else Color(0xFF111827)) else (if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563)),
                    unfocusedTextColor      = if (selected != null) (if (isDark) White else Color(0xFF111827)) else (if (isDark) White.copy(alpha = 0.5f) else Color(0xFF4B5563))
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 15.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontFamily = InterFont) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}