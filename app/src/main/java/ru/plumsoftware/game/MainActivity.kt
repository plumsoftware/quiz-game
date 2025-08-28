package ru.plumsoftware.game

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.yandex.mobile.ads.common.MobileAds
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.GameViewModel
import ru.plumsoftware.game.ui.screens.*
import ru.plumsoftware.game.ui.theme.ExtendedTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        FirebaseApp.initializeApp(this)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task -> }

        setContent {
            ExtendedTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                ) { innerPadding ->
                    GameApp(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        viewModel.navigateUp(this@MainActivity)
    }
}

@Composable
fun GameApp(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val tasksProgress by viewModel.tasksProgress.collectAsState()
    val showQuizResult by viewModel.showQuizResult.collectAsState()
    val quizResult by viewModel.quizResult.collectAsState()
    val availableQuizzes by viewModel.availableQuizzes.collectAsState()
    val currentQuizLevel by viewModel.currentQuizLevel.collectAsState()

    val displayAds by remember { mutableStateOf(Firebase.remoteConfig.getBoolean("display_ads")) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    if (hasNotificationPermission) {
        viewModel.getNotificationScheduler().cancelAllNotifications()
        viewModel.getNotificationScheduler().scheduleDailyNotification()
        viewModel.getNotificationScheduler().scheduleQuizReminder()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when {
        currentScreen == GameScreen.SPLASH -> {
            SplashScreen(
                onSplashComplete = viewModel::onSplashComplete
            )
        }

        showQuizResult -> {
            QuizResultScreen(
                correctAnswers = quizResult.correctAnswers,
                totalQuestions = quizResult.totalQuestions,
                coinsEarned = quizResult.coinsEarned,
                currentLevel = currentQuizLevel,
                onBackToHome = viewModel::onBackToHome,
                onPlayAgain = viewModel::onPlayAgain,
                displayAds = displayAds
            )
        }

        currentScreen == GameScreen.HOME -> {
            HomeScreen(
                gameState = gameState,
                availableQuizzes = availableQuizzes,
                onNavigateToQuiz = { viewModel.navigateTo(GameScreen.QUIZ_MENU) },
                onNavigateToDailyTasks = { viewModel.navigateTo(GameScreen.DAILY_TASKS) },
                onNavigateToShop = { viewModel.navigateTo(GameScreen.SHOP) },
                onNavigateToProfile = { viewModel.navigateTo(GameScreen.PROFILE) },
                onNavigateToSettings = { viewModel.navigateTo(GameScreen.SETTINGS) },
                onNavigateToAchievements = { viewModel.navigateTo(GameScreen.ACHIEVEMENTS) }
            )
        }

        currentScreen == GameScreen.QUIZ_MENU -> {
            val completedQuizzes by viewModel.completedQuizzes.collectAsState()
            QuizMenuScreen(
                gameState = gameState,
                availableQuizzes = availableQuizzes,
                completedQuizzes = completedQuizzes,
                onNavigateToQuiz = { quizId ->
                    viewModel.setCurrentQuizLevel(quizId)
                    viewModel.navigateTo(GameScreen.QUIZ)
                },
                onBack = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }

        currentScreen == GameScreen.QUIZ -> {
            QuizScreen(
                currentLevel = currentQuizLevel,
                questions = viewModel.getQuestionsForCurrentQuiz(),
                onBack = { viewModel.navigateTo(GameScreen.QUIZ_MENU) },
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
                addCoins = viewModel::onAdsRewarded,
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                onPurchase = viewModel::onPurchaseItem
            )
        }

        currentScreen == GameScreen.PROFILE -> {
            ProfileScreen(
                gameState = gameState,
                stats = tasksProgress,
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                onNavigateToSettings = { viewModel.navigateTo(GameScreen.SETTINGS) }
            )
        }

        currentScreen == GameScreen.SETTINGS -> {
            SettingsScreen(
                addCoins = viewModel::onAdsRewarded,
                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                notificationScheduler = viewModel.getNotificationScheduler()
            )
        }

        currentScreen == GameScreen.ACHIEVEMENTS -> {
            AchievementsScreen(
                gameState = gameState,
                onNavigateToSettings = { viewModel.navigateTo(GameScreen.SETTINGS) }
            )
        }
    }
}