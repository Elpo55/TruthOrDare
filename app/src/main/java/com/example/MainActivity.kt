package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.StatsAndHistoryScreen
import com.example.viewmodel.GameViewModel

enum class NavigationScreen {
    SETUP,
    GAME,
    STATS
}

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalAnimationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val gameViewModel: GameViewModel = viewModel()
        val isStarted by gameViewModel.isGameStarted.collectAsState()

        var currentScreen by remember { mutableStateOf(NavigationScreen.SETUP) }

        // Sync ViewModel start session state
        LaunchedEffect(isStarted) {
            currentScreen = if (isStarted) NavigationScreen.GAME else NavigationScreen.SETUP
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
          AnimatedContent(
             targetState = currentScreen,
             transitionSpec = {
                 fadeIn() with fadeOut()
             },
             modifier = Modifier.padding(paddingValues),
             label = "screenRouter"
          ) { screen ->
             when (screen) {
                 NavigationScreen.SETUP -> {
                     SetupScreen(
                         viewModel = gameViewModel,
                         onStartGame = {
                             gameViewModel.startGame()
                             currentScreen = NavigationScreen.GAME
                         }
                     )
                 }
                 NavigationScreen.GAME -> {
                     GameScreen(
                         viewModel = gameViewModel,
                         onOpenStats = {
                             currentScreen = NavigationScreen.STATS
                         },
                         onBackToSetup = {
                             gameViewModel.resetGame()
                             currentScreen = NavigationScreen.SETUP
                         }
                     )
                 }
                 NavigationScreen.STATS -> {
                     StatsAndHistoryScreen(
                         viewModel = gameViewModel,
                         onBackToGame = {
                             currentScreen = NavigationScreen.GAME
                         }
                     )
                 }
             }
          }
        }
      }
    }
  }
}

