package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.ui.components.LiquidBackground
import com.example.ui.components.glassCard
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.TaskViewModel
import com.example.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Initialize Room Database & repository locally using standard native pattern
            val context = LocalContext.current
            val database = remember { TaskDatabase.getDatabase(context) }
            val repository = remember { TaskRepository(database.taskDao()) }
            val factory = remember { TaskViewModelFactory(repository) }

            // Construct ViewModel using our factory
            val taskViewModel: TaskViewModel = viewModel(factory = factory)

            val currentScreen by taskViewModel.currentScreen.collectAsState()
            val isDarkMode by taskViewModel.isDarkMode.collectAsState()
            val toastMessage by taskViewModel.toastMessage.collectAsState()

            // Display Toast notifications matching core design demands
            LaunchedEffect(toastMessage) {
                toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    taskViewModel.clearToast()
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                // Set Liquid glass animated blobs background across ALL layers with notch paddings
                LiquidBackground(
                    isDark = isDarkMode,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                            },
                            label = "screenNavigator"
                        ) { screen ->
                            when (screen) {
                                AppScreen.Splash -> {
                                    SplashScreen(viewModel = taskViewModel, isDark = isDarkMode)
                                }
                                AppScreen.Onboarding -> {
                                    OnboardingScreen(viewModel = taskViewModel, isDark = isDarkMode)
                                }
                                AppScreen.Home -> {
                                    HomeScreen(viewModel = taskViewModel, isDark = isDarkMode)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
