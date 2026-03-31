package com.example.smartvoice.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartvoice.R
import com.example.smartvoice.ui.theme.BrightBlue
import com.example.smartvoice.ui.theme.LogoBlue
import com.example.smartvoice.ui.theme.White

private val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

data class TutorialStep(
    val title: String,
    val description: String,
    val iconRes: Int
)

val homeTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.welcome,
        title = "Welcome to SmartVoice",
        description = "SmartVoice helps you record and analyse voice samples to detect the presence of Recurrent Respiratory Papillomatosis. Let's show you around."
    ),
    TutorialStep(
        iconRes = R.drawable.record,
        title = "Record",
        description = "Tap Record to capture a voice sample. Ask the patient to sustain a steady vowel sound like 'aah' or 'eee'."
    ),
    TutorialStep(
        iconRes = R.drawable.results,
        title = "Results",
        description = "After recording, head to Results to view the analysis. Each sample is marked as Analysing, Ready, or Failed."
    ),
    TutorialStep(
        iconRes = R.drawable.accountinfo,
        title = "Account Info",
        description = "Manage your account and any additional adults linked to it. You can view and edit personal details here."
    ),
    TutorialStep(
        iconRes = R.drawable.childinfo,
        title = "Child Info",
        description = "Add and manage child profiles. Each recording is linked to a child so results are always organised."
    ),
    TutorialStep(
        iconRes = R.drawable.faqs,
        title = "FAQs",
        description = "Find answers to common questions and quick access to NHS 111 and 999 if you need urgent medical help."
    ),
    TutorialStep(
        iconRes = R.drawable.feedback,
        title = "Feedback",
        description = "Let us know how we're doing. Your feedback helps us improve SmartVoice for everyone."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "You're all set!",
        description = "That's everything. You can replay this tutorial at any time by tapping the ? button in the top right corner on each page."
    )
)

val recordTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.record,
        title = "Record",
        description = "Use this page to capture a new voice sample for analysis."
    ),
    TutorialStep(
        iconRes = R.drawable.record,
        title = "Before Recording",
        description = "Ask the patient to hold a steady vowel sound like 'aah' or 'eee' in a quiet space."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

val resultsTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.results,
        title = "Results",
        description = "This page shows the status and outcome of each submitted voice sample."
    ),
    TutorialStep(
        iconRes = R.drawable.results,
        title = "Sample Status",
        description = "A recording may appear as Analysing, Ready, or Failed depending on whether processing is still in progress or complete."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

val accountInfoTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.accountinfo,
        title = "Account Info",
        description = "Use this page to view and manage your account details."
    ),
    TutorialStep(
        iconRes = R.drawable.accountinfo,
        title = "Adults on the Account",
        description = "You can manage the main account holder and any additional adults linked to the account here."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

val childInfoTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.childinfo,
        title = "Child Info",
        description = "Use this page to add and manage child profiles linked to the account."
    ),
    TutorialStep(
        iconRes = R.drawable.childinfo,
        title = "Why this matters",
        description = "Each voice recording is attached to a child profile so results stay organised and easy to review."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

val faqsTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.faqs,
        title = "FAQs",
        description = "This page answers common questions about SmartVoice and how to use it."
    ),
    TutorialStep(
        iconRes = R.drawable.faqs,
        title = "Getting support",
        description = "You can also use this page to quickly access NHS 111 or 999 if urgent medical help is needed."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

val feedbackTutorialSteps = listOf(
    TutorialStep(
        iconRes = R.drawable.feedback,
        title = "Feedback",
        description = "Use this page to share your thoughts and experiences with SmartVoice."
    ),
    TutorialStep(
        iconRes = R.drawable.feedback,
        title = "Why feedback matters",
        description = "Your feedback helps improve the app and make it more helpful for future users."
    ),
    TutorialStep(
        iconRes = R.drawable.getstarted,
        title = "Need help later?",
        description = "You can reopen this page guide any time by tapping the ? button in the top right corner."
    )
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    onFinish: () -> Unit
) {
    var currentStep by remember(steps) { mutableStateOf(0) }
    val step = steps[currentStep]
    val isLast = currentStep == steps.lastIndex
    val isFirst = currentStep == 0

    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .pointerInput(Unit) { detectTapGestures {} },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 10.dp else 7.dp)
                                .background(
                                    color = when {
                                        index == currentStep -> White
                                        index < currentStep -> BrightBlue
                                        else -> White.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = White, shape = RoundedCornerShape(28.dp))
                        .padding(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = step.iconRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .offset(y = 10.dp),
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.tint(LogoBlue)
                            )
                        }

                        Text(
                            text = step.title,
                            fontFamily = InterFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = LogoBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = step.description,
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { if (isLast) onFinish() else currentStep++ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = if (isLast) "Got It" else "Next",
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isFirst) {
                                    TextButton(onClick = { currentStep-- }) {
                                        Text(
                                            text = "← Back",
                                            fontFamily = InterFont,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = LogoBlue
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                if (!isLast) {
                                    TextButton(onClick = onFinish) {
                                        Text(
                                            text = "Skip",
                                            fontFamily = InterFont,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}