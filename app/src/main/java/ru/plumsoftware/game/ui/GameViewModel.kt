package ru.plumsoftware.game.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.findQuizIdForCategory
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.Question
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.notifications.NotificationScheduler
import ru.plumsoftware.game.ui.components.game.AchievementToast
import ru.plumsoftware.game.ui.components.game.toToast
import ru.plumsoftware.game.ui.screens.getAchievements
import java.util.ArrayDeque

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameManager = GameManager(application)
    private val notificationScheduler = NotificationScheduler(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _tasksProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val tasksProgress: StateFlow<Map<String, Int>> = _tasksProgress.asStateFlow()

    private val _currentScreen = MutableStateFlow(GameScreen.SPLASH)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    private val _showQuizResult = MutableStateFlow(false)
    val showQuizResult: StateFlow<Boolean> = _showQuizResult.asStateFlow()

    private val _quizResult = MutableStateFlow(QuizResult(0, 0, 0))
    val quizResult: StateFlow<QuizResult> = _quizResult.asStateFlow()

    private val _currentQuizLevel = MutableStateFlow(1)
    val currentQuizLevel: StateFlow<Int> = _currentQuizLevel.asStateFlow()

    private val _availableQuizzes =
        MutableStateFlow<List<Quiz>>(emptyList())
    val availableQuizzes: StateFlow<List<Quiz>> =
        _availableQuizzes.asStateFlow()

    private val _completedQuizzes = MutableStateFlow<Set<Int>>(emptySet())
    val completedQuizzes: StateFlow<Set<Int>> = _completedQuizzes.asStateFlow()

    private val _finishedQuizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val finishedQuizzes: StateFlow<List<Quiz>> = _finishedQuizzes.asStateFlow()

    private val _currentTierQuizTotal = MutableStateFlow(0)
    val currentTierQuizTotal: StateFlow<Int> = _currentTierQuizTotal.asStateFlow()

    private val _remoteQuiz = MutableStateFlow(
        RemoteConfigQuizModel(
            cardTitle = "",
            dateStart = "",
            dateEnd = "",
            quiz = Quiz(
                0, "", "", "", 0, 0, emptyList()
            )
        )
    )
    val remoteQuiz: StateFlow<RemoteConfigQuizModel> =
        _remoteQuiz.asStateFlow()

    private val _shopOpenedFromQuiz = MutableStateFlow(false)
    val shopOpenedFromQuiz: StateFlow<Boolean> = _shopOpenedFromQuiz.asStateFlow()

    private val _pendingAchievementToast = MutableStateFlow<AchievementToast?>(null)
    val pendingAchievementToast: StateFlow<AchievementToast?> = _pendingAchievementToast.asStateFlow()

    private val achievementToastQueue = ArrayDeque<AchievementToast>()
    private var isShowingAchievementToast = false

    init {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                _gameState.value = state
                refreshQuizLists(state.unlockedQuizLevels)
            }
        }

        viewModelScope.launch {
            gameManager.getDailyTasksProgress().collect { progress ->
                _tasksProgress.value = progress
            }
        }
    }

    private suspend fun refreshQuizLists(unlockedTier: Int) {
        val completedIds = mutableSetOf<Int>()
        val finished = mutableListOf<Quiz>()
        for (quiz in GameData.getAllQuizzes()) {
            if (gameManager.isQuizCompleted(quiz.id).first()) {
                completedIds.add(quiz.id)
                finished.add(quiz)
            }
        }
        _completedQuizzes.value = completedIds
        _finishedQuizzes.value = finished.sortedBy { it.requiredLevel }

        val tierQuizzes = GameData.getAllQuizzes().filter { it.requiredLevel == unlockedTier }
        _currentTierQuizTotal.value = tierQuizzes.size
        _availableQuizzes.value = tierQuizzes.filter { it.id !in completedIds }
    }

    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }

    fun setCurrentQuizLevel(level: Int) {
        _currentQuizLevel.value = level
    }

    fun setRemoteConfigQuizLevel(remoteQuiz: RemoteConfigQuizModel) {
        _remoteQuiz.value = remoteQuiz
    }

    fun onQuizComplete(correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val currentQuizId = _currentQuizLevel.value
            val currentQuiz = GameData.getQuiz(currentQuizId)
            val difficultyMultiplier = when (currentQuiz?.difficulty) {
                1 -> 1.0
                2 -> 1.5
                3 -> 2.0
                4 -> 2.5
                5 -> 3.0
                6 -> 3.5
                else -> 1.0
            }

            val baseCoins = correctAnswers * 10
            val bonusCoins = if (correctAnswers == totalQuestions) 50 else 0
            val coinsEarned = ((baseCoins + bonusCoins) * difficultyMultiplier).toInt()

            // Update game state
            gameManager.addCoins(coinsEarned)
            gameManager.addExperience(correctAnswers * 5 * (currentQuiz?.difficulty ?: 1))
            gameManager.incrementQuizzesCompleted()
            gameManager.addCorrectAnswers(correctAnswers)
            gameManager.addTotalAnswers(totalQuestions)
            gameManager.updateLastPlayDate()

            // Mark quiz as completed if all answers are correct
            if (correctAnswers == totalQuestions) {
                gameManager.completeQuiz(currentQuizId)
            }

            // Add play time (estimate 2 minutes per quiz)
            gameManager.addPlayTime(2)

            // Add categories played
            currentQuiz?.let { quiz ->
                gameManager.addCategoryPlayed(quiz.category)
            }

            _quizResult.value = QuizResult(correctAnswers, totalQuestions, coinsEarned)
            _showQuizResult.value = true
            checkNewAchievements()
        }
    }

    private suspend fun checkNewAchievements() {
        val state = gameManager.gameState.first()
        val unlocked = gameManager.getUnlockedAchievements()
        getAchievements(state)
            .filter { it.current >= it.target && it.id !in unlocked }
            .forEach { achievement ->
                gameManager.unlockAchievement(achievement.id)
                if (achievement.reward > 0) {
                    gameManager.addCoins(achievement.reward)
                }
                achievementToastQueue.addLast(achievement.toToast())
            }
        if (!isShowingAchievementToast) {
            showNextAchievementToast()
        }
    }

    private fun showNextAchievementToast() {
        if (achievementToastQueue.isEmpty()) {
            isShowingAchievementToast = false
            return
        }
        isShowingAchievementToast = true
        _pendingAchievementToast.value = achievementToastQueue.removeFirst()
    }

    fun dismissAchievementToast() {
        viewModelScope.launch {
            _pendingAchievementToast.value = null
            delay(400)
            showNextAchievementToast()
        }
    }

    fun startQuizForCategory(categoryId: String) {
        val quizId = findQuizIdForCategory(categoryId)
        if (quizId != null) {
            setCurrentQuizLevel(quizId)
            navigateTo(GameScreen.QUIZ)
        } else {
            navigateTo(GameScreen.QUIZ_MENU)
        }
    }

    fun onAdsRewarded(reward: Int) {
        viewModelScope.launch {
            if (reward == 1)
                gameManager.addCoins(50)
            else
                gameManager.addCoins(reward)
        }
    }

    fun addCoins(coinsEarned: Int) {
        viewModelScope.launch {
            gameManager.addCoins(coinsEarned)
        }
    }

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            gameManager.updatePlayerName(name)
        }
    }

    fun onPurchaseItem(itemId: Int, price: Int) {
        viewModelScope.launch {
            gameManager.addCoins(-price)
        }
    }

    fun purchasePowerUp(type: PowerUpType, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = gameManager.purchasePowerUp(type)
            onResult(success)
        }
    }

    fun consumePowerUp(type: PowerUpType, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = gameManager.consumePowerUp(type)
            onResult(success)
        }
    }

    fun openShopFromQuiz() {
        _shopOpenedFromQuiz.value = true
        _currentScreen.value = GameScreen.SHOP
    }

    fun closeShop() {
        if (_shopOpenedFromQuiz.value) {
            _shopOpenedFromQuiz.value = false
            _currentScreen.value = GameScreen.QUIZ
        } else {
            _currentScreen.value = GameScreen.HOME
        }
    }

    fun onBackToHome() {
        _showQuizResult.value = false
        _currentScreen.value = GameScreen.HOME
    }

    fun onPlayAgain() {
        _showQuizResult.value = false
        _currentScreen.value = GameScreen.QUIZ
    }

    fun onSplashComplete() {
        _currentScreen.value = GameScreen.HOME
    }

    fun navigateUp(activity: Activity) {
        when (_currentScreen.value) {
            GameScreen.QUIZ -> _currentScreen.value = GameScreen.QUIZ_MENU
            GameScreen.QUIZ_MENU -> _currentScreen.value = GameScreen.HOME
            GameScreen.MORE -> _currentScreen.value = GameScreen.HOME
            GameScreen.DAILY_TASKS -> _currentScreen.value = GameScreen.HOME
            GameScreen.SHOP -> closeShop()
            GameScreen.STATS -> _currentScreen.value = GameScreen.HOME
            GameScreen.SETTINGS -> _currentScreen.value = GameScreen.HOME
            GameScreen.ACHIEVEMENTS -> _currentScreen.value = GameScreen.HOME
            GameScreen.CATEGORIES -> _currentScreen.value = GameScreen.HOME
            else -> {
                activity.finish()
            }
        }
    }

    fun getNotificationScheduler(): NotificationScheduler {
        return notificationScheduler
    }

    fun getQuestionsForCurrentQuiz(): List<ru.plumsoftware.game.data.Question> {
        return _remoteQuiz.value.quiz.questions.ifEmpty {
            GameData.getQuestionsForQuiz(_currentQuizLevel.value)
        }
    }

    fun setEmptyRemoteQuiz() {
        _remoteQuiz.value = RemoteConfigQuizModel(
            "",
            "",
            "",
            Quiz(
                0, "", "", "", 0, 0, emptyList()
            )
        )
    }

    fun canPlayQuiz(quizId: Int): Boolean {
        val gameState = _gameState.value
        val quiz = GameData.getQuiz(quizId)
        return quiz?.requiredLevel ?: 0 <= gameState.unlockedQuizLevels
    }
}

enum class GameScreen {
    SPLASH,
    HOME,
    QUIZ_MENU,
    QUIZ,
    DAILY_TASKS,
    SHOP,
    STATS,
    SETTINGS,
    ACHIEVEMENTS,
    MORE,
    CATEGORIES
}

data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val coinsEarned: Int
) 