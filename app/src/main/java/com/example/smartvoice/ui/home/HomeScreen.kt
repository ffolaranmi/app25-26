package com.example.smartvoice.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.smartvoice.R
import com.example.smartvoice.ui.history.HistoryDestination
import com.example.smartvoice.ui.navigation.NavigationDestination
import com.example.smartvoice.ui.child.ChildInfoDestination
import com.example.smartvoice.ui.record.RecordDestination
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
object AccountInfoDestination : NavigationDestination {
    override val route = "accountInfo"
    override val titleRes = R.string.app_name
}
object FindMedicalHelpDestination : NavigationDestination {
    override val route = "findMedicalHelp"
    override val titleRes = R.string.app_name
}

object FeedbackDestination : NavigationDestination {
    override val route = "feedback"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToScreenOption: (NavigationDestination) -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
) {
    GradientBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(72.dp))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp,
                    letterSpacing = (-1.2).sp,
                    color = LogoBlue
                )

                Spacer(modifier = Modifier.height(20.dp))

                BubbleCluster(
                    onBubbleClick = navigateToScreenOption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                )

                Text(
                    text = "Created by Engineering and Humanities \nstudents at the University of Strathclyde.",
                    textAlign = TextAlign.Center,
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B6B),
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                LogoutButton(navigateToLogin)

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "University of Strathclyde 2026",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun BubbleCluster(
    onBubbleClick: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Bubble(
            size = 155.dp,
            icon = Icons.Outlined.MenuBook,
            onClick = { onBubbleClick(HistoryDestination) },
            modifier = Modifier.offset(x = 0.dp, y = (-50).dp)
        )

        Bubble(
            size = 112.dp,
            icon = Icons.Outlined.MicNone,
            onClick = { onBubbleClick(RecordDestination) },
            modifier = Modifier.offset(x = (-118).dp, y = (-138).dp)
        )

        Bubble(
            size = 112.dp,
            icon = Icons.Outlined.HelpOutline,
            onClick = { onBubbleClick(FindMedicalHelpDestination) },
            modifier = Modifier.offset(x = (118).dp, y = (-138).dp)
        )

        Bubble(
            size = 135.dp,
            icon = Icons.Outlined.PeopleOutline,
            onClick = { onBubbleClick(ChildInfoDestination) },
            modifier = Modifier.offset(x = (-118).dp, y = (52).dp)
        )

        Bubble(
            size = 135.dp,
            icon = Icons.Outlined.PersonOutline,
            onClick = { onBubbleClick(AccountInfoDestination) },
            modifier = Modifier.offset(x = (118).dp, y = (52).dp)
        )

        Bubble(
            size = 95.dp,
            icon = Icons.Outlined.ChatBubbleOutline,
            onClick = { onBubbleClick(FeedbackDestination) },
            modifier = Modifier.offset(x = 0.dp, y = (118).dp)
        )
    }
}

@Composable
private fun Bubble(
    size: Dp,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(BrightBlue)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

@Composable
private fun LogoutButton(navigateToLogin: () -> Unit) {
    Button(
        onClick = { navigateToLogin() },
        modifier = Modifier
            .width(240.dp)
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB71C1C)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = "Logout",
            fontFamily = InterFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}