package com.example.smartvoice.ui.child

import com.example.smartvoice.ui.child.ChildViewModel
import com.example.smartvoice.ui.child.ChildViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Phone
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
import com.example.smartvoice.data.HospitalData
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.components.SmartVoiceBottomBar
import com.example.smartvoice.ui.components.SmartVoiceTopBar
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import java.util.Calendar
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.input.KeyboardType

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

private val TileTextColor = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private const val OTHER_HOSPITAL_ID = "OTHER"

private val hospitalOptions = mapOf(
    "1001" to "Queen Elizabeth University Hospital, ENT",
    "1002" to "Aberdeen Royal Infirmary, ENT",
    "1003" to "St Johns Hospital Livingstone, ENT",
    "1004" to "Ninewells Hospital, ENT",
    "1005" to "University Hospital Monklands, ENT",
    "1006" to "Forth Valley Royal Hospital, ENT",
    OTHER_HOSPITAL_ID to "Other"
)

private enum class OpenDropdown { NONE, GENDER, MONTH, HOSPITAL }

private fun calculateAge(birthMonth: Int, birthYear: Int): String {
    val now = Calendar.getInstance()
    val curYear = now.get(Calendar.YEAR)
    val curMonth = now.get(Calendar.MONTH) + 1
    var age = curYear - birthYear
    if (curMonth < birthMonth) age--
    return if (age < 0) "Unknown" else "$age years old"
}

private val genderOptions = listOf("Female", "Male")

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

private fun formatCustomHospitalValue(customHospital: String): String {
    return "OTHER:${customHospital.trim()}"
}

private fun isCustomHospitalValue(hospitalId: String): Boolean {
    return hospitalId.startsWith("OTHER:")
}

private fun extractCustomHospitalName(hospitalId: String): String {
    return if (isCustomHospitalValue(hospitalId)) hospitalId.removePrefix("OTHER:") else ""
}

@Composable
fun ChildInfoScreen(
    database: SmartVoiceDatabase,
    navigateBack: () -> Unit,
    navigateToChildDetail: (Long) -> Unit,
) {
    val context = LocalContext.current
    val userId = remember { SessionPrefs.getLoggedInUserId(context) }
    val viewModel: ChildViewModel = viewModel(factory = ChildViewModelFactory(database))
    val children by viewModel.children.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<ChildTable?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { viewModel.loadChildren(userId) }

    showDeleteDialog?.let { child ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Remove Child?", fontFamily = InterFont, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you sure you want to remove ${child.firstName} ${child.lastName}?", fontFamily = InterFont)
                    Text("This action cannot be undone.", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = ErrorRed)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteChild(child); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Remove", fontFamily = InterFont, color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", fontFamily = InterFont, color = LogoBlue)
                }
            }
        )
    }

    if (showAddDialog) {
        AddChildDialog(
            accountHolderId = userId,
            onDismiss = { showAddDialog = false },
            onConfirm = { child -> viewModel.addChild(child); showAddDialog = false }
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
                SmartVoiceTopBar(title = "Child Info", onBack = navigateBack)
                Spacer(modifier = Modifier.height(16.dp))

                if (children.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No children added yet.\nTap '+ Add Child' to get started.",
                            fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 16.sp,
                            color = PlaceholderColor, textAlign = TextAlign.Center, lineHeight = 24.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
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

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Child", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                SmartVoiceBottomBar(onHomeClick = navigateBack)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ChildListTile(child: ChildTable, onClick: () -> Unit, onDeleteClick: () -> Unit) {
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
                Text("${child.firstName} ${child.lastName}", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TileTextColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(calculateAge(child.birthMonth, child.birthYear), fontFamily = InterFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = PlaceholderColor)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = LogoBlue.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove child", tint = ErrorRed, modifier = Modifier.size(20.dp))
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
) {
    val context = LocalContext.current
    val viewModel: ChildViewModel = viewModel(factory = ChildViewModelFactory(database))
    val child by viewModel.selectedChild.collectAsState()
    var showManageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(childId) { viewModel.loadChildById(childId) }

    if (showManageDialog) {
        child?.let { c ->
            EditChildDialog(
                child = c,
                onDismiss = { showManageDialog = false },
                onConfirm = { updated -> viewModel.updateChild(updated); showManageDialog = false }
            )
        }
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                SmartVoiceTopBar(title = child?.let { "${it.firstName} ${it.lastName}" } ?: "Child", onBack = navigateBack)
                Spacer(modifier = Modifier.height(20.dp))

                child?.let { c ->
                    InfoTileRow(label = "First Name", value = c.firstName)
                    InfoTileRow(label = "Last Name", value = c.lastName)
                    InfoTileRow(label = "Gender", value = c.gender)
                    InfoTileRow(label = "Birth Year & Month", value = "${monthNames.getOrNull(c.birthMonth - 1) ?: c.birthMonth} ${c.birthYear}")
                    InfoTileRow(label = "Age", value = calculateAge(c.birthMonth, c.birthYear))
                    HospitalInfoTile(label = "Hospital", hospitalId = c.hospitalId, context = context)
                } ?: Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrightBlue)
                }

                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = { showManageDialog = true },
                    modifier = Modifier.width(220.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Manage Child Info", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White) }

                Spacer(modifier = Modifier.weight(1f))
                SmartVoiceBottomBar(onHomeClick = navigateHome)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InfoTileRow(label: String, value: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(label, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = PlaceholderColor, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value.ifEmpty { "—" }, fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (value.isNotEmpty()) TileTextColor else PlaceholderColor)
        }
    }
}

@Composable
private fun HospitalInfoTile(label: String, hospitalId: String, context: android.content.Context) {
    val isCustomHospital = isCustomHospitalValue(hospitalId)
    val hospitalName = if (isCustomHospital) extractCustomHospitalName(hospitalId) else HospitalData.getHospitalName(hospitalId)
    val hospitalPhone = if (isCustomHospital) "" else HospitalData.getHospitalPhone(hospitalId)

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .background(color = PillGrey, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = PlaceholderColor, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(hospitalName.ifEmpty { "—" }, fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (hospitalName.isNotEmpty()) TileTextColor else PlaceholderColor)
            }
            if (hospitalPhone.isNotEmpty()) {
                IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$hospitalPhone"))) }, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Filled.Phone, contentDescription = "Call hospital", tint = BrightBlue, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AddChildDialog(accountHolderId: Long, onDismiss: () -> Unit, onConfirm: (ChildTable) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var birthYear by remember { mutableStateOf("") }
    var selectedHospital by remember { mutableStateOf<String?>(null) }
    var customHospital by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var openDropdown by remember { mutableStateOf(OpenDropdown.NONE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        title = { Text("Add Child", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LogoBlue) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(value = firstName, label = "First Name", onValueChange = { firstName = filterNameInput(it) })
                DialogField(value = lastName, label = "Last Name", onValueChange = { lastName = filterNameInput(it) })
                SimpleDropdownField(label = "Gender", options = genderOptions, selected = selectedGender, isExpanded = openDropdown == OpenDropdown.GENDER, onExpandChange = { openDropdown = if (it) OpenDropdown.GENDER else OpenDropdown.NONE }, onSelected = { selectedGender = it; openDropdown = OpenDropdown.NONE })
                MonthDropdownField(selectedMonth = selectedMonth, isExpanded = openDropdown == OpenDropdown.MONTH, onExpandChange = { openDropdown = if (it) OpenDropdown.MONTH else OpenDropdown.NONE }, onMonthSelected = { selectedMonth = it; openDropdown = OpenDropdown.NONE }, label = "Birth Month")
                DialogField(value = birthYear, label = "Birth Year", keyboardType = KeyboardType.Number, onValueChange = { birthYear = it.filter { ch -> ch.isDigit() }.take(4) })
                HospitalDropdownField(selectedHospital = selectedHospital, isExpanded = openDropdown == OpenDropdown.HOSPITAL, onExpandChange = { openDropdown = if (it) OpenDropdown.HOSPITAL else OpenDropdown.NONE }, onHospitalSelected = { selectedHospital = it; openDropdown = OpenDropdown.NONE; if (it != OTHER_HOSPITAL_ID) customHospital = "" })
                if (selectedHospital == OTHER_HOSPITAL_ID) {
                    DialogField(value = customHospital, label = "Enter Hospital Name", onValueChange = { customHospital = it })
                }
                if (error.isNotEmpty()) Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = birthYear.toIntOrNull()
                    val month = selectedMonth
                    val gender = selectedGender
                    val hospital = selectedHospital
                    val now = Calendar.getInstance()
                    val curYear = now.get(Calendar.YEAR)
                    val curMonth = now.get(Calendar.MONTH) + 1
                    val minAllowedYear = curYear - 17
                    val finalHospitalValue = when {
                        hospital == OTHER_HOSPITAL_ID && customHospital.trim().isNotBlank() -> formatCustomHospitalValue(customHospital)
                        hospital != null -> hospital
                        else -> null
                    }
                    when {
                        firstName.trim().isBlank() || lastName.trim().isBlank() -> error = "Please enter a first and last name."
                        gender.isNullOrBlank() -> error = "Please select a gender."
                        month == null -> error = "Please select a birth month."
                        year == null || year < 1900 -> error = "Please enter a valid birth year."
                        hospital.isNullOrBlank() -> error = "Please select a hospital."
                        hospital == OTHER_HOSPITAL_ID && customHospital.trim().isBlank() -> error = "Please enter the hospital name."
                        year > curYear || (year == curYear && month > curMonth) -> error = "Birth month and year cannot be in the future."
                        year < minAllowedYear -> error = "This app is for children 17 years and under."
                        year == minAllowedYear && month < curMonth -> error = "This app is for children 17 years and under."
                        else -> onConfirm(ChildTable(userId = accountHolderId, firstName = firstName.trim(), lastName = lastName.trim(), gender = gender, birthMonth = month, birthYear = year, hospitalId = finalHospitalValue ?: ""))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Add", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) } }
    )
}

@Composable
private fun EditChildDialog(child: ChildTable, onDismiss: () -> Unit, onConfirm: (ChildTable) -> Unit) {
    var firstName by remember { mutableStateOf(child.firstName) }
    var lastName by remember { mutableStateOf(child.lastName) }
    var selectedGender by remember { mutableStateOf(child.gender.takeIf { it.isNotBlank() }) }
    var selectedMonth by remember { mutableStateOf(child.birthMonth) }
    var birthYear by remember { mutableStateOf(child.birthYear.toString()) }
    var selectedHospital by remember {
        mutableStateOf(when { isCustomHospitalValue(child.hospitalId) -> OTHER_HOSPITAL_ID; child.hospitalId.isNotBlank() -> child.hospitalId; else -> null })
    }
    var customHospital by remember { mutableStateOf(if (isCustomHospitalValue(child.hospitalId)) extractCustomHospitalName(child.hospitalId) else "") }
    var error by remember { mutableStateOf("") }
    var openDropdown by remember { mutableStateOf(OpenDropdown.NONE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        title = { Text("Edit Child Info", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LogoBlue) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(value = firstName, label = "First Name", onValueChange = { firstName = filterNameInput(it) })
                DialogField(value = lastName, label = "Last Name", onValueChange = { lastName = filterNameInput(it) })
                SimpleDropdownField(label = "Gender", options = genderOptions, selected = selectedGender, isExpanded = openDropdown == OpenDropdown.GENDER, onExpandChange = { openDropdown = if (it) OpenDropdown.GENDER else OpenDropdown.NONE }, onSelected = { selectedGender = it; openDropdown = OpenDropdown.NONE })
                MonthDropdownField(selectedMonth = selectedMonth, isExpanded = openDropdown == OpenDropdown.MONTH, onExpandChange = { openDropdown = if (it) OpenDropdown.MONTH else OpenDropdown.NONE }, onMonthSelected = { selectedMonth = it; openDropdown = OpenDropdown.NONE }, label = "Birth Month")
                DialogField(value = birthYear, label = "Birth Year", keyboardType = KeyboardType.Number, onValueChange = { birthYear = it.filter { ch -> ch.isDigit() }.take(4) })
                HospitalDropdownField(selectedHospital = selectedHospital, isExpanded = openDropdown == OpenDropdown.HOSPITAL, onExpandChange = { openDropdown = if (it) OpenDropdown.HOSPITAL else OpenDropdown.NONE }, onHospitalSelected = { selectedHospital = it; openDropdown = OpenDropdown.NONE; if (it != OTHER_HOSPITAL_ID) customHospital = "" })
                if (selectedHospital == OTHER_HOSPITAL_ID) {
                    DialogField(value = customHospital, label = "Enter Hospital Name", onValueChange = { customHospital = it })
                }
                if (error.isNotEmpty()) Text(error, fontFamily = InterFont, fontSize = 12.sp, color = ErrorRed)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = birthYear.toIntOrNull()
                    val gender = selectedGender
                    val hospital = selectedHospital
                    val now = Calendar.getInstance()
                    val curYear = now.get(Calendar.YEAR)
                    val curMonth = now.get(Calendar.MONTH) + 1
                    val minAllowedYear = curYear - 17
                    val finalHospitalValue = when {
                        hospital == OTHER_HOSPITAL_ID && customHospital.trim().isNotBlank() -> formatCustomHospitalValue(customHospital)
                        hospital != null -> hospital
                        else -> null
                    }
                    when {
                        firstName.trim().isBlank() || lastName.trim().isBlank() -> error = "Please enter a first and last name."
                        gender.isNullOrBlank() -> error = "Please select a gender."
                        selectedMonth !in 1..12 -> error = "Please select a birth month."
                        year == null || year < 1900 -> error = "Please enter a valid birth year."
                        hospital.isNullOrBlank() -> error = "Please select a hospital."
                        hospital == OTHER_HOSPITAL_ID && customHospital.trim().isBlank() -> error = "Please enter the hospital name."
                        year > curYear || (year == curYear && selectedMonth > curMonth) -> error = "Birth month and year cannot be in the future."
                        year < minAllowedYear -> error = "This app is for children 17 years and under."
                        year == minAllowedYear && selectedMonth < curMonth -> error = "This app is for children 17 years and under."
                        else -> onConfirm(child.copy(firstName = firstName.trim(), lastName = lastName.trim(), gender = gender, birthMonth = selectedMonth, birthYear = year, hospitalId = finalHospitalValue ?: ""))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Save", fontFamily = InterFont, fontWeight = FontWeight.Bold, color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = InterFont, color = LogoBlue) } }
    )
}

@Composable
private fun DialogField(value: String, label: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TileTextColor, unfocusedTextColor = TileTextColor),
            textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdownField(selectedMonth: Int?, isExpanded: Boolean, onExpandChange: (Boolean) -> Unit, onMonthSelected: (Int) -> Unit, label: String) {
    val displayText = selectedMonth?.let { monthNames.getOrNull(it - 1) } ?: label
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { onExpandChange(it) }, modifier = Modifier.fillMaxWidth()) {
            TextField(value = displayText, onValueChange = {}, readOnly = true,
                placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = if (selectedMonth != null) TileTextColor else PlaceholderColor, unfocusedTextColor = if (selectedMonth != null) TileTextColor else PlaceholderColor),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { onExpandChange(false) }) {
                monthNames.forEachIndexed { index, month -> DropdownMenuItem(text = { Text(month, fontFamily = InterFont) }, onClick = { onMonthSelected(index + 1) }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownField(label: String, options: List<String>, selected: String?, isExpanded: Boolean, onExpandChange: (Boolean) -> Unit, onSelected: (String) -> Unit) {
    val displayText = selected ?: label
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { onExpandChange(it) }, modifier = Modifier.fillMaxWidth()) {
            TextField(value = displayText, onValueChange = {}, readOnly = true,
                placeholder = { Text(label, fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = if (selected != null) TileTextColor else PlaceholderColor, unfocusedTextColor = if (selected != null) TileTextColor else PlaceholderColor),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { onExpandChange(false) }) {
                options.forEach { item -> DropdownMenuItem(text = { Text(item, fontFamily = InterFont) }, onClick = { onSelected(item) }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HospitalDropdownField(selectedHospital: String?, isExpanded: Boolean, onExpandChange: (Boolean) -> Unit, onHospitalSelected: (String) -> Unit) {
    val displayText = when { selectedHospital == null -> "Select Hospital"; selectedHospital == OTHER_HOSPITAL_ID -> "Other"; else -> hospitalOptions[selectedHospital] ?: "Select Hospital" }
    Box(modifier = Modifier.fillMaxWidth().background(color = PillGrey, shape = RoundedCornerShape(12.dp))) {
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { onExpandChange(it) }, modifier = Modifier.fillMaxWidth()) {
            TextField(value = displayText, onValueChange = {}, readOnly = true,
                placeholder = { Text("Select Hospital", fontFamily = InterFont, color = PlaceholderColor, fontSize = 14.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = if (selectedHospital != null) TileTextColor else PlaceholderColor, unfocusedTextColor = if (selectedHospital != null) TileTextColor else PlaceholderColor),
                textStyle = LocalTextStyle.current.copy(fontFamily = InterFont, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { onExpandChange(false) }) {
                hospitalOptions.forEach { (id, name) -> DropdownMenuItem(text = { Text(name, fontFamily = InterFont) }, onClick = { onHospitalSelected(id) }) }
            }
        }
    }
}