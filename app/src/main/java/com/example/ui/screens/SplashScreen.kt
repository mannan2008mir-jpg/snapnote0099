package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glassCard
import com.example.viewmodel.AppScreen
import com.example.viewmodel.TaskViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: TaskViewModel, isDark: Boolean) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2200)
        viewModel.navigateTo(AppScreen.Onboarding)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleAnim)
        ) {
            // Glass Logo Circular Wrapper
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFCA72FF).copy(alpha = 0.35f), Color.Transparent),
                            radius = 150f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .glassCard(isDark = isDark, cornerRadius = 22.dp, opacity = 0.3f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "SnapNote Logo icon",
                        tint = Color(0xFFFFD84D),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SnapNote",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF2A1B3D),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Capture instantly. Core productivity.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF2A1B3D).copy(alpha = 0.7f)
            )
        }
    }
}
