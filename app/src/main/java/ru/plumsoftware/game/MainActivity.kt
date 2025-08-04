package ru.plumsoftware.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.GameViewModel
import ru.plumsoftware.game.ui.screens.*
import ru.plumsoftware.game.ui.theme.GameTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameApp(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun GameApp(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val tasksProgress by viewModel.tasksProgress.collectAsState()
    val showQuizResult by viewModel.showQuizResult.collectAsState()
    val quizResult by viewModel.quizResult.collectAsState()

    when {
        showQuizResult -> {
            QuizResultScreen(
                correctAnswers = quizResult.correctAnswers,
                totalQuestions = quizResult.totalQuestions,
                coinsEarned = quizResult.coinsEarned,
                onBackToHome = viewModel::onBackToHome,
                onPlayAgain = viewModel::onPlayAgain
            )
        }

        currentScreen == GameScreen.HOME -> {
            HomeScreen(
                gameState = gameState,
                onNavigateToQuiz = { viewModel.navigateTo(GameScreen.QUIZ) },
                onNavigateToDailyTasks = { viewModel.navigateTo(GameScreen.DAILY_TASKS) },
                onNavigateToShop = { viewModel.navigateTo(GameScreen.SHOP) },
                onNavigateToProfile = { viewModel.navigateTo(GameScreen.PROFILE) }
            )
        }

        currentScreen == GameScreen.QUIZ -> {
            QuizScreen(
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                onQuizComplete = viewModel::onQuizComplete
            )
        }

        currentScreen == GameScreen.DAILY_TASKS -> {
            DailyTasksScreen(
                tasksProgress = tasksProgress,
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                onTaskCompleted = { _, _ -> /* Handle task completion */ }
            )
        }

        currentScreen == GameScreen.SHOP -> {
            ShopScreen(
                coins = gameState.coins,
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                onPurchase = viewModel::onPurchaseItem
            )
        }

        currentScreen == GameScreen.PROFILE -> {
            ProfileScreen(
                gameState = gameState,
                stats = tasksProgress,
                onBack = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }
    }
}