package ru.plumsoftware.game

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plumsoftware.game.ads.RewardedAdController
import ru.plumsoftware.game.audio.GameFeedback
import ru.plumsoftware.game.audio.LocalFeedback
import ru.plumsoftware.game.audio.Sfx
import ru.plumsoftware.game.data.Achievements
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.GameViewModel
import ru.plumsoftware.game.ui.components.MainBack
import ru.plumsoftware.game.ui.components.kids.AchievementToastOverlay
import ru.plumsoftware.game.ui.components.kids.AdBanner
import ru.plumsoftware.game.ui.components.kids.BottomTab
import ru.plumsoftware.game.ui.components.kids.KidsBottomNav
import ru.plumsoftware.game.ui.components.kids.KidsToast
import ru.plumsoftware.game.ui.components.kids.ParentalGate
import ru.plumsoftware.game.ui.screens.AchievementsScreen
import ru.plumsoftware.game.ui.screens.ChestRewardDialog
import ru.plumsoftware.game.ui.screens.HomeScreen
import ru.plumsoftware.game.ui.screens.LevelUpDialog
import ru.plumsoftware.game.ui.screens.MilestoneDialog
import ru.plumsoftware.game.ui.screens.PrivacyScreen
import ru.plumsoftware.game.ui.screens.ProfileScreen
import ru.plumsoftware.game.ui.screens.QuizResultScreen
import ru.plumsoftware.game.ui.screens.QuizScreen
import ru.plumsoftware.game.ui.screens.SettingsScreen
import ru.plumsoftware.game.ui.screens.ShopScreen
import ru.plumsoftware.game.ui.screens.SignupScreen
import ru.plumsoftware.game.ui.screens.StreakScreen
import ru.plumsoftware.game.ui.screens.TimeLimitScreen
import ru.plumsoftware.game.ui.screens.TopicsScreen
import ru.plumsoftware.game.ui.screens.WelcomeScreen
import ru.plumsoftware.game.ui.theme.ExtendedTheme
import ru.plumsoftware.game.ui.theme.Kids

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    private lateinit var feedback: GameFeedback
    private lateinit var rewarded: RewardedAdController

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Системный сплэш держим, пока не прочитан профиль (холодный старт ≤ 2 с, §13).
        splashScreen.setKeepOnScreenCondition { viewModel.currentScreen.value == GameScreen.SPLASH }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        feedback = GameFeedback(this)
        rewarded = RewardedAdController(this, StoreConfig.rewardedAdUnitId)
        rewarded.load()

        // Учёт времени игры для дневного лимита (§9.2): считаем, пока приложение на экране.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(15_000L)
                    viewModel.addPlayTime(15)
                }
            }
        }

        setContent {
            ExtendedTheme {
                CompositionLocalProvider(LocalFeedback provides feedback) {
                    MainBack {
                        GameApp(
                            viewModel = viewModel,
                            activity = this,
                            feedback = feedback,
                            rewarded = rewarded
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        feedback.onForeground(true)
    }

    override fun onStop() {
        super.onStop()
        feedback.onForeground(false)
        feedback.stopSpeaking()
    }

    override fun onDestroy() {
        super.onDestroy()
        rewarded.destroy()
        feedback.release()
    }

    fun appVersion(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: ""
    } catch (e: Exception) {
        ""
    }

    fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            viewModel.showToast("Не удалось открыть ссылку")
        }
    }

    /** «Оценить приложение» — страница в магазине площадки сборки (RuStore и т.д.). */
    fun rateApp() = openUrl(StoreConfig.storeAppUrl)
}

@Composable
fun GameApp(
    viewModel: GameViewModel,
    activity: MainActivity,
    feedback: GameFeedback,
    rewarded: RewardedAdController
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val session by viewModel.session.collectAsState()
    val result by viewModel.result.collectAsState()
    val levelUp by viewModel.levelUp.collectAsState()
    val milestone by viewModel.milestone.collectAsState()
    val chestReward by viewModel.chestReward.collectAsState()
    val toast by viewModel.toast.collectAsState()
    val achievementToast by viewModel.pendingAchievementToast.collectAsState()

    // Настройки звука/музыки/вибрации/озвучки.
    LaunchedEffect(gameState.settings) { feedback.applySettings(gameState.settings) }
    // Музыка приглушается на время видеорекламы (§10).
    LaunchedEffect(rewarded.isShowing) { feedback.setMusicSuppressed(rewarded.isShowing) }


    // Разрешение на уведомления (Android 13+) — после создания профиля.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var askedPermission by rememberSaveable { mutableStateOf(false) }
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(currentScreen, gameState.settings.notifications) {
        if (currentScreen == GameScreen.HOME && gameState.settings.notifications && !askedPermission) {
            askedPermission = true
            requestNotificationPermission()
        }
    }

    BackHandler { if (!viewModel.navigateUp()) activity.finish() }

    var showLimitGate by remember { mutableStateOf(false) }
    val limitReached = gameState.profileCreated && gameState.timeLimitReached() &&
        currentScreen != GameScreen.QUIZ && result == null

    // На планшете контент по центру, ширина до 600 (§13).
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.fillMaxSize().widthIn(max = 600.dp)) {
            val currentResult = result
            if (currentResult != null) {
                Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    QuizResultScreen(
                        result = currentResult,
                        avatarId = gameState.avatarId,
                        goldenFrame = gameState.goldenFrame,
                        rewardedReady = rewarded.isLoaded,
                        onDoubleReward = {
                            val shown = rewarded.show(onRewarded = { viewModel.onDoubleRewardEarned() })
                            if (!shown) viewModel.showToast("Видео пока нет")
                        },
                        onNextLevel = viewModel::nextLevel,
                        onReplay = viewModel::replayLevel,
                        onToMap = viewModel::backToMap
                    )
                }
            } else {
                val tab = when (currentScreen) {
                    GameScreen.HOME -> BottomTab.HOME
                    GameScreen.TOPICS -> BottomTab.TOPICS
                    GameScreen.SHOP -> BottomTab.SHOP
                    GameScreen.PROFILE -> BottomTab.PROFILE
                    else -> null
                }
                // Edge-to-edge: фон тянется под системные панели. Если есть нижнее меню,
                // его белый фон уходит под навигационную панель, а отступ — внутри меню.
                Column(
                    Modifier.fillMaxSize().statusBarsPadding()
                        .then(if (tab == null) Modifier.navigationBarsPadding() else Modifier)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        transitionSpec = {
                            if (initialState == GameScreen.SPLASH) {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            } else {
                                val dir = if (targetState.ordinal > initialState.ordinal)
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else AnimatedContentTransitionScope.SlideDirection.Right
                                slideIntoContainer(dir, tween(300)) togetherWith slideOutOfContainer(dir, tween(300))
                            }
                        },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            GameScreen.SPLASH -> Box(Modifier.fillMaxSize())
                            GameScreen.WELCOME -> WelcomeScreen(
                                onStart = { viewModel.navigateTo(GameScreen.SIGNUP) }
                            )
                            GameScreen.SIGNUP -> SignupScreen(
                                onBack = { viewModel.navigateTo(GameScreen.WELCOME) },
                                onFinish = { name, avatarId, ageGroup -> viewModel.createProfile(name, avatarId, ageGroup) }
                            )
                            GameScreen.HOME -> HomeScreen(
                                gameState = gameState,
                                onOpenStreak = { viewModel.navigateTo(GameScreen.STREAK) },
                                onOpenTopics = { viewModel.navigateTo(GameScreen.TOPICS) },
                                onClaimQuest = {
                                    feedback.play(Sfx.COIN)
                                    viewModel.claimDailyQuest()
                                },
                                onNodeClick = viewModel::onMapLevelClick,
                                onOpenChest = viewModel::openChest
                            )
                            GameScreen.STREAK -> StreakScreen(
                                gameState = gameState,
                                onBack = { viewModel.navigateTo(GameScreen.HOME) },
                                onOpenShop = { viewModel.navigateTo(GameScreen.SHOP) }
                            )
                            GameScreen.QUIZ -> {
                                val s = session
                                if (s == null) Box(Modifier.fillMaxSize())
                                else QuizScreen(
                                    session = s,
                                    coins = gameState.coins,
                                    powerUpInventory = gameState.powerUpInventory,
                                    onExit = viewModel::exitQuiz,
                                    onFinish = { stats -> viewModel.onLevelFinished(stats) },
                                    onConsumeHint = { type, cb -> viewModel.consumeHint(type, cb) },
                                    onBuyHint = { type, cb -> viewModel.buyHint(type, cb) }
                                )
                            }
                            GameScreen.TOPICS -> TopicsScreen(
                                gameState = gameState,
                                playableTopics = viewModel.playableTopics,
                                onSelectTopic = viewModel::setCurrentTopic,
                                onSelectDifficulty = viewModel::setDifficulty
                            )
                            GameScreen.SHOP -> ShopScreen(
                                gameState = gameState,
                                rewardedReady = rewarded.isLoaded,
                                onFreeCoins = {
                                    val shown = rewarded.show(onRewarded = { viewModel.onFreeCoinsEarned() })
                                    if (!shown) viewModel.showToast("Видео пока нет")
                                },
                                onBuyHint = { type, cb -> viewModel.buyHint(type, cb) },
                                onBuyGemItem = { item, cb -> viewModel.buyGemItem(item, cb) },
                                onAvatar = { avatar, cb -> viewModel.buyOrSelectAvatar(avatar, cb) },
                                onToast = viewModel::showToast
                            )
                            GameScreen.PROFILE -> ProfileScreen(
                                gameState = gameState,
                                achievements = Achievements.evaluate(gameState, viewModel.playableTopics),
                                onOpenSettings = { viewModel.navigateTo(GameScreen.SETTINGS) },
                                onOpenAchievements = { viewModel.navigateTo(GameScreen.ACHIEVEMENTS) }
                            )
                            GameScreen.ACHIEVEMENTS -> AchievementsScreen(
                                achievements = Achievements.evaluate(gameState, viewModel.playableTopics),
                                onBack = { viewModel.navigateTo(GameScreen.PROFILE) }
                            )
                            GameScreen.SETTINGS -> SettingsScreen(
                                settings = gameState.settings,
                                appVersion = activity.appVersion(),
                                onChange = viewModel::updateSettings,
                                onRemindersEnabled = { requestNotificationPermission() },
                                onOpenPrivacy = { viewModel.navigateTo(GameScreen.PRIVACY) },
                                onRateApp = { activity.rateApp() },
                                onBack = { viewModel.navigateTo(GameScreen.PROFILE) }
                            )
                            GameScreen.PRIVACY -> PrivacyScreen(onBack = { viewModel.navigateTo(GameScreen.SETTINGS) })
                        }
                    }
                    // Баннер 320×50 — только на главной, над нижним меню (§8).
                    if (currentScreen == GameScreen.HOME) {
                        AdBanner(StoreConfig.bannerAdUnitId)
                    }
                    if (tab != null) Box(Modifier.fillMaxWidth().background(Kids.Card)) {
                        KidsBottomNav(
                            modifier = Modifier.navigationBarsPadding(),
                            selected = tab,
                            onSelect = { t ->
                                feedback.play(Sfx.TAP)
                                viewModel.navigateTo(
                                    when (t) {
                                        BottomTab.HOME -> GameScreen.HOME
                                        BottomTab.TOPICS -> GameScreen.TOPICS
                                        BottomTab.SHOP -> GameScreen.SHOP
                                        BottomTab.PROFILE -> GameScreen.PROFILE
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Лимит времени: «На сегодня всё!» (§9.2).
            if (limitReached) {
                Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    TimeLimitScreen(onExtend = { showLimitGate = true })
                }
                if (showLimitGate) {
                    ParentalGate(
                        onPass = {
                            showLimitGate = false
                            viewModel.extendTimeLimit(15)
                        },
                        onDismiss = { showLimitGate = false }
                    )
                }
            }

            chestReward?.let { ChestRewardDialog(it, onDismiss = viewModel::dismissChest) }
            val lu = levelUp
            val ms = milestone
            if (result != null && lu != null) {
                LevelUpDialog(lu, onDismiss = viewModel::dismissLevelUp)
            } else if (result != null && ms != null) {
                MilestoneDialog(ms, onDismiss = viewModel::dismissMilestone)
            }

            KidsToast(
                message = toast,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
            )
        }

        AchievementToastOverlay(
            toast = achievementToast,
            onShown = { feedback.play(Sfx.ACHIEVEMENT) },
            onDismiss = viewModel::dismissAchievementToast,
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp)
        )
    }
}
