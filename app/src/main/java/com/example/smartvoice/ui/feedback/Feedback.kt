package com.example.smartvoice.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Favorite
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
import androidx.navigation.NavController
import com.example.smartvoice.R
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.ErrorRed
import com.example.smartvoice.ui.theme.GradientBackground
import com.example.smartvoice.ui.theme.LightBlue
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.DarkPill
import com.example.smartvoice.ui.theme.DarkTextPrimary
import com.example.smartvoice.ui.theme.DarkTextSecondary
import com.example.smartvoice.ui.theme.PillGrey
import com.example.smartvoice.ui.theme.White
import android.content.Intent
import android.net.Uri
import com.example.smartvoice.ui.navigation.NavigationDestination

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

object FeedbackDestination : NavigationDestination {
    override val route = "feedback"
    override val titleRes = R.string.feedback
}

private val TileTextColor = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF4B5563)

@Composable
fun FeedbackScreen(
    navController: NavController,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit
) {
    val context = LocalContext.current

    val googleFormUrl = "https://forms.gle/sVdSmjXAgaHF9UA77"

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
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
                            tint = if (isSystemInDarkTheme()) White else LogoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "Feedback",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-2.5).sp,
                        color = if (isSystemInDarkTheme()) White else LogoBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) DarkPill else PillGrey
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Thank You for Using SmartVoice!",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = if (isSystemInDarkTheme()) DarkTextPrimary else TileTextColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "We value your opinion and would love to hear about your experience with the app. Your feedback helps us make SmartVoice better for everyone.",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = if (isSystemInDarkTheme()) DarkTextSecondary else PlaceholderColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleFormUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrightBlue,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Open Feedback Form",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You'll be redirected to a Google Form.",
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) DarkTextSecondary else PlaceholderColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "SmartVoice",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-1.5).sp,
                    color = if (isSystemInDarkTheme()) White else LogoBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}