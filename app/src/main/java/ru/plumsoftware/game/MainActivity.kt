package ru.plumsoftware.game

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.yandex.mobile.ads.common.MobileAds
import kotlinx.serialization.json.Json
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.GameViewModel
import ru.plumsoftware.game.ui.components.MainBack
import ru.plumsoftware.game.ui.components.game.AchievementToastOverlay
import ru.plumsoftware.game.ui.screens.*
import ru.plumsoftware.game.ui.theme.ExtendedTheme
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val keepSplash = mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition { keepSplash.value }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        MobileAds.initialize(this) {}
        FirebaseApp.initializeApp(this)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        setContent {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500L)
                keepSplash.value = false
            }

            ExtendedTheme {
                MainBack {
                    GameApp(viewModel = viewModel)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
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
    val completedQuizzes by viewModel.completedQuizzes.collectAsState()
    val finishedQuizzes by viewModel.finishedQuizzes.collectAsState()
    val currentTierQuizTotal by viewModel.currentTierQuizTotal.collectAsState()
    val pendingAchievementToast by viewModel.pendingAchievementToast.collectAsState()

    val displayAds by remember { mutableStateOf(true) }
    val remoteQuiz by remember {
        mutableStateOf(
            try {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }.decodeFromString<RemoteConfigQuizModel>(
                    Firebase.remoteConfig["time_quiz"].asString()
                )
            } catch (e: Exception) {
                RemoteConfigQuizModel(
                    cardTitle = "",
                    dateEnd = "01.01.2000 12:00",
                    dateStart = "01.01.2000 12:00",
                    quiz = Quiz(
                        id = 0, title = "", description = "", category = "",
                        difficulty = 0, requiredLevel = 0, questions = emptyList()
                    )
                )
            }
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    if (hasNotificationPermission) {
        viewModel.getNotificationScheduler().cancelAllNotifications()
        viewModel.getNotificationScheduler().scheduleDailyNotification()
        viewModel.getNotificationScheduler().scheduleQuizReminder()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasNotificationPermission = isGranted }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (showQuizResult) {
            Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
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
        } else {
            AnimatedContent(
                targetState = currentScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                transitionSpec = {
                    val slideDirection = if (targetState.ordinal > initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    slideIntoContainer(slideDirection, tween(300)) togetherWith
                        slideOutOfContainer(slideDirection, tween(300))
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    GameScreen.SPLASH -> SplashScreen(onSplashComplete = viewModel::onSplashComplete)
                    GameScreen.HOME -> HomeScreen(
                        remoteQuiz = remoteQuiz,
                        gameState = gameState,
                        availableQuizzes = availableQuizzes,
                        currentTierQuizTotal = currentTierQuizTotal,
                        tasksProgress = tasksProgress,
                        onNavigateToQuiz = { viewModel.navigateTo(GameScreen.QUIZ_MENU) },
                        onNavigateToDailyTasks = { viewModel.navigateTo(GameScreen.DAILY_TASKS) },
                        onNavigateToShop = { viewModel.navigateTo(GameScreen.SHOP) },
                        onNavigateToAchievements = { viewModel.navigateTo(GameScreen.ACHIEVEMENTS) },
                        onNavigateToStats = { viewModel.navigateTo(GameScreen.STATS) },
                        onNavigateToSettings = { viewModel.navigateTo(GameScreen.SETTINGS) },
                        onNavigateToRemoteConfigQuiz = { rq ->
                            viewModel.setRemoteConfigQuizLevel(rq)
                            viewModel.navigateTo(GameScreen.QUIZ)
                        }
                    )
                    GameScreen.MORE -> MoreScreen(onNavigate = viewModel::navigateTo)
                    GameScreen.CATEGORIES -> CategoriesScreen(
                        onBack = { viewModel.navigateTo(GameScreen.HOME) },
                        onCategorySelect = { viewModel.startQuizForCategory(it.id) }
                    )
                    GameScreen.QUIZ_MENU -> QuizMenuScreen(
                        gameState = gameState,
                        availableQuizzes = availableQuizzes,
                        finishedQuizzes = finishedQuizzes,
                        completedQuizzes = completedQuizzes,
                        onNavigateToQuiz = { quizId ->
                            viewModel.setCurrentQuizLevel(quizId)
                            viewModel.navigateTo(GameScreen.QUIZ)
                        },
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                    GameScreen.QUIZ -> QuizScreen(
                        currentLevel = currentQuizLevel,
                        questions = viewModel.getQuestionsForCurrentQuiz(),
                        coins = gameState.coins,
                        powerUpInventory = gameState.powerUpInventory,
                        onBack = {
                            viewModel.setEmptyRemoteQuiz()
                            viewModel.navigateTo(GameScreen.QUIZ_MENU)
                        },
                        onQuizComplete = viewModel::onQuizComplete,
                        onPurchasePowerUp = viewModel::purchasePowerUp,
                        onConsumePowerUp = viewModel::consumePowerUp
                    )
                    GameScreen.DAILY_TASKS -> DailyTasksScreen(
                        tasksProgress = tasksProgress,
                        onBack = { viewModel.navigateTo(GameScreen.HOME) },
                        onTaskCompleted = { _, reward -> viewModel.addCoins(reward) }
                    )
                    GameScreen.SHOP -> ShopScreen(
                        coins = gameState.coins,
                        inventory = gameState.powerUpInventory,
                        addCoins = viewModel::onAdsRewarded,
                        onBack = viewModel::closeShop,
                        onPurchasePowerUp = viewModel::purchasePowerUp
                    )
                    GameScreen.STATS -> StatsScreen(
                        gameState = gameState,
                        stats = tasksProgress,
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                    GameScreen.SETTINGS -> SettingsScreen(
                        addCoins = viewModel::onAdsRewarded,
                        onBack = { viewModel.navigateTo(GameScreen.HOME) },
                        notificationScheduler = viewModel.getNotificationScheduler()
                    )
                    GameScreen.ACHIEVEMENTS -> AchievementsScreen(
                        gameState = gameState,
                        onBack = { viewModel.navigateTo(GameScreen.HOME) },
                        onNavigateToSettings = { viewModel.navigateTo(GameScreen.SETTINGS) }
                    )
                }
            }
        }

        AchievementToastOverlay(
            toast = pendingAchievementToast,
            onDismiss = viewModel::dismissAchievementToast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
