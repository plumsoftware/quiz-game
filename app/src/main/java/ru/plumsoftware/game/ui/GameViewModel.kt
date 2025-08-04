package ru.plumsoftware.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.GameState

class GameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val gameManager = GameManager(application)
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _tasksProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val tasksProgress: StateFlow<Map<String, Int>> = _tasksProgress.asStateFlow()
    
    private val _currentScreen = MutableStateFlow(GameScreen.HOME)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()
    
    private val _showQuizResult = MutableStateFlow(false)
    val showQuizResult: StateFlow<Boolean> = _showQuizResult.asStateFlow()
    
    private val _quizResult = MutableStateFlow(QuizResult(0, 0, 0))
    val quizResult: StateFlow<QuizResult> = _quizResult.asStateFlow()
    
    init {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                _gameState.value = state
            }
        }
        
        viewModelScope.launch {
            gameManager.getDailyTasksProgress().collect { progress ->
                _tasksProgress.value = progress
            }
        }
    }
    
    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }
    
    fun onQuizComplete(correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val coinsEarned = correctAnswers * 10 + (if (correctAnswers == totalQuestions) 50 else 0)
            
            // Update game state
            gameManager.addCoins(coinsEarned)
            gameManager.addExperience(correctAnswers * 5)
            gameManager.incrementQuizzesCompleted()
            gameManager.addCorrectAnswers(correctAnswers)
            gameManager.updateLastPlayDate()
            
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
}

enum class GameScreen {
    HOME,
    QUIZ,
    DAILY_TASKS,
    SHOP,
    PROFILE
}

data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val coinsEarned: Int
) 