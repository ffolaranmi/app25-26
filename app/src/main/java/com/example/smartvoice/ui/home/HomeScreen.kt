package com.example.smartvoice.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvoice.R
import com.example.smartvoice.data.SessionPrefs
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.ui.account.AccountInfoDestination
import com.example.smartvoice.ui.account.AccountInfoViewModel
import com.example.smartvoice.ui.account.AccountInfoViewModelFactory
import com.example.smartvoice.ui.child.ChildInfoDestination
import com.example.smartvoice.ui.child.ChildViewModel
import com.example.smartvoice.ui.child.ChildViewModelFactory
import com.example.smartvoice.ui.faqs.FaqsDestination
import com.example.smartvoice.ui.feedback.FeedbackDestination
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.record.RecordDestination
import com.example.smartvoice.ui.results.ResultsDestination
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.White

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToScreenOption: (NavigationDestination) -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
    database: SmartVoiceDatabase? = null,
) {
    val context = LocalContext.current
    val userId  = remember { SessionPrefs.getLoggedInUserId(context) }

    val accountVm: AccountInfoViewModel? = if (database != null) {
        viewModel(factory = AccountInfoViewModelFactory(database, context))
    } else null

    val childVm: ChildViewModel? = if (database != null) {
        viewModel(factory = ChildViewModelFactory(database))
    } else null

    val accountHolder by accountVm?.user?.collectAsState() ?: remember { mutableStateOf(null) }
    val children      by childVm?.children?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var showFirstLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != -1L) childVm?.loadChildren(userId)
    }

    LaunchedEffect(accountHolder) {
        if (accountHolder?.firstLoginFlag == true && children.isEmpty()) {
            showFirstLoginDialog = true
        }
    }

    if (showFirstLoginDialog && accountHolder != null) {
        AlertDialog(
            onDismissRequest = { },
            shape = RoundedCornerShape(20.dp),
            containerColor = White,
            title = null,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = BrightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.childinfo),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            colorFilter = ColorFilter.tint(White)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Add Your Child", fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LogoBlue)
                        Text(
                            "Organize voice recordings and track results for your child.",
                            fontFamily = InterFont, fontSize = 13.sp, color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center, lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        accountVm?.markFirstLoginComplete(accountHolder!!)
                        showFirstLoginDialog = false
                        navigateToScreenOption(ChildInfoDestination)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Child", fontFamily = InterFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { accountVm?.markFirstLoginComplete(accountHolder!!); showFirstLoginDialog = false },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Maybe Later", fontFamily = InterFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF9CA3AF))
                }
            }
        )
    }

    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = (-1.2).sp,
                    color = LogoBlue
                )

                Spacer(modifier = Modifier.height(28.dp))

                TileGrid(
                    onTileClick = navigateToScreenOption,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Created by Engineering and Humanities\nstudents at the University of Strathclyde.",
                    textAlign = TextAlign.Center,
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 20.dp).padding(horizontal = 10.dp).fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                LogoutButton(navigateToLogin)

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "University of Strathclyde 2026",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun TileGrid(
    onTileClick: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tile(
                drawableRes = R.drawable.record,
                title = "RECORD",
                onClick = { onTileClick(RecordDestination) },
                modifier = Modifier.weight(1f)
            )

            Tile(
                drawableRes = R.drawable.results,
                title = "RESULTS",
                onClick = { onTileClick(ResultsDestination) },
                modifier = Modifier.weight(1f)
            )

        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tile(
                drawableRes = R.drawable.accountinfo,
                title = "ACCOUNT",
                onClick = { onTileClick(AccountInfoDestination) },
                modifier = Modifier.weight(1f)
            )
            Tile(
                drawableRes = R.drawable.childinfo,
                title = "CHILD INFO",
                onClick = { onTileClick(ChildInfoDestination) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tile(
                drawableRes = R.drawable.faqs,
                title = "FAQS",
                onClick = { onTileClick(FaqsDestination) },
                modifier = Modifier.weight(1f)
            )
            Tile(
                drawableRes = R.drawable.feedback,
                title = "FEEDBACK",
                onClick = { onTileClick(FeedbackDestination) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun Tile(
    drawableRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(modifier),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(125.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    color = BrightBlue.copy(alpha = 0.1f)
                )
                .clickable(
                    indication = ripple(bounded = true, color = BrightBlue.copy(alpha = 0.2f)),
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.9f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(BrightBlue.copy(alpha = 0.8f))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontFamily = InterFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = LogoBlue,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun LogoutButton(navigateToLogin: () -> Unit) {
    Button(
        onClick = { navigateToLogin() },
        modifier = Modifier
            .width(200.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
        shape = RoundedCornerShape(12.dp)

    ) {
        Text(text = "Logout", fontFamily = InterFont, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}