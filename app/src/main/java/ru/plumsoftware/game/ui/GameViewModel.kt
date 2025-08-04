package ru.plumsoftware.game.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.times
import androidx.compose.ui.unit.times
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.notifications.NotificationScheduler
import kotlin.system.exitProcess
import kotlin.time.times

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
    
    private val _availableQuizLevels = MutableStateFlow<List<ru.plumsoftware.game.data.QuizLevel>>(emptyList())
    val availableQuizLevels: StateFlow<List<ru.plumsoftware.game.data.QuizLevel>> = _availableQuizLevels.asStateFlow()
    
    init {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                _gameState.value = state
                updateAvailableQuizLevels(state.unlockedQuizLevels)
            }
        }
        
        viewModelScope.launch {
            gameManager.getDailyTasksProgress().collect { progress ->
                _tasksProgress.value = progress
            }
        }
    }
    
    private fun updateAvailableQuizLevels(unlockedLevels: Int) {
        val availableLevels = mutableListOf<ru.plumsoftware.game.data.QuizLevel>()
        for (i in 1..unlockedLevels) {
            GameData.getQuizLevel(i)?.let { level ->
                availableLevels.add(level)
            }
        }
        _availableQuizLevels.value = availableLevels
    }
    
    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }
    
    fun setCurrentQuizLevel(level: Int) {
        _currentQuizLevel.value = level
    }
    
    fun onQuizComplete(correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val currentLevel = _currentQuizLevel.value
            val levelMultiplier = when (currentLevel) {
                1 -> 1.0
                2 -> 1.5
                3 -> 2.0
                else -> 1.0
            }
            
            val baseCoins = correctAnswers * 10
            val bonusCoins = if (correctAnswers == totalQuestions) 50 else 0
            val coinsEarned = ((baseCoins + bonusCoins) * levelMultiplier).toInt()
            
            // Update game state
            gameManager.addCoins(coinsEarned)
            gameManager.addExperience(correctAnswers * 5 * currentLevel)
            gameManager.incrementQuizzesCompleted()
            gameManager.addCorrectAnswers(correctAnswers)
            gameManager.addTotalAnswers(totalQuestions)
            gameManager.updateLastPlayDate()
            
            // Mark quiz level as completed if all answers are correct
            if (correctAnswers == totalQuestions) {
                gameManager.completeQuizLevel(currentLevel)
            }
            
            // Add play time (estimate 2 minutes per quiz)
            gameManager.addPlayTime(2)
            
            // Add categories played (simplified - just add one category per quiz)
            gameManager.addCategoryPlayed("General")
            
            _quizResult.value = QuizResult(correctAnswers, totalQuestions, coinsEarned)
            _showQuizResult.value = true
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
            GameScreen.QUIZ -> _currentScreen.value = GameScreen.HOME
            GameScreen.DAILY_TASKS -> _currentScreen.value = GameScreen.HOME
            GameScreen.SHOP -> _currentScreen.value = GameScreen.HOME
            GameScreen.PROFILE -> _currentScreen.value = GameScreen.HOME
            GameScreen.SETTINGS -> _currentScreen.value = GameScreen.HOME
            GameScreen.ACHIEVEMENTS -> _currentScreen.value = GameScreen.HOME
            else -> { activity.finish() }
        }
    }
    
    fun getNotificationScheduler(): NotificationScheduler {
        return notificationScheduler
    }
    
    fun getQuestionsForCurrentLevel(): List<ru.plumsoftware.game.data.Question> {
        return GameData.getQuestionsForLevel(_currentQuizLevel.value)
    }
    
    fun canPlayQuizLevel(level: Int): Boolean {
        val gameState = _gameState.value
        return level <= gameState.unlockedQuizLevels
    }
}

enum class GameScreen {
    SPLASH,
    HOME,
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