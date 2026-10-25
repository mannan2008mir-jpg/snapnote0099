package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glassCard
import com.example.viewmodel.AppScreen
import com.example.viewmodel.TaskViewModel

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val tag: String
)

@Composable
fun OnboardingScreen(viewModel: TaskViewModel, isDark: Boolean) {
    var currentPageIndex by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Fast Note Capturing",
            description = "Quickly log tasks, assign priority levels, and track deadlines using our responsive floating interface.",
            icon = Icons.Default.Assignment,
            iconColor = Color(0xFFCA72FF),
            tag = "First page"
        ),
        OnboardingPage(
            title = "Productive Statistics",
            description = "Stay in charge of your goals. Observe daily completion percentages, pending task rates, and task metrics in real-time.",
            icon = Icons.Default.DoneAll,
            iconColor = Color(0xFFFFD84D),
            tag = "Second page"
        )
    )

    val page = pages[currentPageIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Glass Panel wrapper
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(isDark = isDark, cornerRadius = 32.dp, opacity = 0.15f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Fade and Slide animated state changes for high-fidelity interactive flow
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "pageTransition"
                ) { targetPage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon circle
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(targetPage.iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = targetPage.icon,
                                contentDescription = "Page Icon",
                                tint = targetPage.iconColor,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = targetPage.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF2A1B3D),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = targetPage.description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF2A1B3D).copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Page indicators and navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                pages.forEachIndexed { index, _ ->
                    val isActive = index == currentPageIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(width = if (isActive) 24.dp else 10.dp, height = 10.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isActive) Color(0xFFCA72FF) else (if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFCA72FF).copy(alpha = 0.2f))
                            )
                            .clickable { currentPageIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Glowing Primary Action Button
            Button(
                onClick = {
                    if (currentPageIndex < pages.size - 1) {
                        currentPageIndex++
                    } else {
                        viewModel.navigateTo(AppScreen.Home)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCA72FF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentPageIndex == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Forward Icon",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
