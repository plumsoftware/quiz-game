package ru.plumsoftware.game.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.Question
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.notifications.NotificationScheduler

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

    init {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                _gameState.value = state
                updateAvailableQuizzes(state.unlockedQuizLevels)
            }
        }

        viewModelScope.launch {
            gameManager.getDailyTasksProgress().collect { progress ->
                _tasksProgress.value = progress
            }
        }

        viewModelScope.launch {
            // Track completed quizzes
            gameManager.gameState.collect { state ->
                val completedQuizzes = mutableSetOf<Int>()
                for (quiz in GameData.getAllQuizzes()) {
                    if (quiz.requiredLevel <= state.unlockedQuizLevels) {
                        val isCompleted = gameManager.isQuizCompleted(quiz.id).first()
                        if (isCompleted) {
                            completedQuizzes.add(quiz.id)
                        }
                    }
                }
                _completedQuizzes.value = completedQuizzes.toSet()
            }
        }
    }

    private fun updateAvailableQuizzes(unlockedLevels: Int) {
        val availableQuizzes = GameData.getAllQuizzes().filter { quiz ->
            quiz.requiredLevel <= unlockedLevels
        }
        _availableQuizzes.value = availableQuizzes
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

    fun onPurchaseItem(itemId: Int, price: Int) {
        viewModelScope.launch {
            gameManager.addCoins(-price)
            // Here you would implement the actual item effects
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
            GameScreen.DAILY_TASKS -> _currentScreen.value = GameScreen.HOME
            GameScreen.SHOP -> _currentScreen.value = GameScreen.HOME
            GameScreen.PROFILE -> _currentScreen.value = GameScreen.HOME
            GameScreen.SETTINGS -> _currentScreen.value = GameScreen.HOME
            GameScreen.ACHIEVEMENTS -> _currentScreen.value = GameScreen.HOME
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
    PROFILE,
    SETTINGS,
    ACHIEVEMENTS
}

data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val coinsEarned: Int
) 