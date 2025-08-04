package ru.plumsoftware.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class GameManager(private val context: Context) {
    
    private object PreferencesKeys {
        val COINS = intPreferencesKey("coins")
        val LEVEL = intPreferencesKey("level")
        val EXPERIENCE = intPreferencesKey("experience")
        val LAST_PLAY_DATE = stringPreferencesKey("last_play_date")
        val DAILY_TASKS_COMPLETED = intPreferencesKey("daily_tasks_completed")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val TOTAL_QUIZZES_COMPLETED = intPreferencesKey("total_quizzes_completed")
        val TOTAL_CORRECT_ANSWERS = intPreferencesKey("total_correct_answers")
        val TOTAL_ANSWERS = intPreferencesKey("total_answers")
        val PLAY_TIME_MINUTES = intPreferencesKey("play_time_minutes")
        val CATEGORIES_PLAYED = stringSetPreferencesKey("categories_played")
        val UNLOCKED_QUIZ_LEVELS = intPreferencesKey("unlocked_quiz_levels")
        val COMPLETED_QUIZ_LEVELS = stringSetPreferencesKey("completed_quiz_levels")
    }

    val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        GameState(
            coins = preferences[PreferencesKeys.COINS] ?: 0,
            level = preferences[PreferencesKeys.LEVEL] ?: 1,
            experience = preferences[PreferencesKeys.EXPERIENCE] ?: 0,
            lastPlayDate = preferences[PreferencesKeys.LAST_PLAY_DATE]?.let { LocalDate.parse(it) },
            dailyTasksCompleted = preferences[PreferencesKeys.DAILY_TASKS_COMPLETED] ?: 0,
            streakDays = preferences[PreferencesKeys.STREAK_DAYS] ?: 0,
            unlockedQuizLevels = preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] ?: 1,
            quizzesCompleted = preferences[PreferencesKeys.TOTAL_QUIZZES_COMPLETED] ?: 0,
            correctAnswers = preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0,
            totalAnswers = preferences[PreferencesKeys.TOTAL_ANSWERS] ?: 0,
            streak = preferences[PreferencesKeys.STREAK_DAYS] ?: 0,
            playTimeMinutes = preferences[PreferencesKeys.PLAY_TIME_MINUTES] ?: 0,
            categoriesPlayed = preferences[PreferencesKeys.CATEGORIES_PLAYED] ?: emptySet()
        )
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val currentCoins = preferences[PreferencesKeys.COINS] ?: 0
            preferences[PreferencesKeys.COINS] = currentCoins + amount
        }
    }

    suspend fun addExperience(amount: Int) {
        context.dataStore.edit { preferences ->
            val currentExp = preferences[PreferencesKeys.EXPERIENCE] ?: 0
            val currentLevel = preferences[PreferencesKeys.LEVEL] ?: 1
            val newExp = currentExp + amount
            
            // Level up every 100 experience points
            val newLevel = (newExp / 100) + 1
            preferences[PreferencesKeys.EXPERIENCE] = newExp
            preferences[PreferencesKeys.LEVEL] = newLevel
            
            // Give bonus coins for leveling up
            if (newLevel > currentLevel) {
                val currentCoins = preferences[PreferencesKeys.COINS] ?: 0
                preferences[PreferencesKeys.COINS] = currentCoins + (newLevel - currentLevel) * 50
            }
            
            // Check if new quiz levels should be unlocked
            checkAndUnlockQuizLevels(newLevel, preferences)
        }
    }

    private suspend fun checkAndUnlockQuizLevels(playerLevel: Int, preferences: MutablePreferences) {
        val currentUnlockedLevels = preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] ?: 1
        
        // Unlock level 2 at player level 3
        if (playerLevel >= 3 && currentUnlockedLevels < 2) {
            preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] = 2
        }
        
        // Unlock level 3 at player level 5
        if (playerLevel >= 5 && currentUnlockedLevels < 3) {
            preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] = 3
        }
    }

    suspend fun updateLastPlayDate() {
        context.dataStore.edit { preferences ->
            val today = LocalDate.now().toString()
            val lastPlayDate = preferences[PreferencesKeys.LAST_PLAY_DATE]
            
            preferences[PreferencesKeys.LAST_PLAY_DATE] = today
            
            // Update streak
            if (lastPlayDate != null) {
                val lastDate = LocalDate.parse(lastPlayDate)
                val todayDate = LocalDate.now()
                
                if (lastDate.plusDays(1) == todayDate) {
                    val currentStreak = preferences[PreferencesKeys.STREAK_DAYS] ?: 0
                    preferences[PreferencesKeys.STREAK_DAYS] = currentStreak + 1
                } else if (lastDate != todayDate) {
                    preferences[PreferencesKeys.STREAK_DAYS] = 1
                }
            } else {
                preferences[PreferencesKeys.STREAK_DAYS] = 1
            }
        }
    }

    suspend fun incrementQuizzesCompleted() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_QUIZZES_COMPLETED] ?: 0
            preferences[PreferencesKeys.TOTAL_QUIZZES_COMPLETED] = current + 1
        }
    }

    suspend fun addCorrectAnswers(count: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0
            preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] = current + count
        }
    }

    suspend fun addTotalAnswers(count: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_ANSWERS] ?: 0
            preferences[PreferencesKeys.TOTAL_ANSWERS] = current + count
        }
    }

    suspend fun addPlayTime(minutes: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PLAY_TIME_MINUTES] ?: 0
            preferences[PreferencesKeys.PLAY_TIME_MINUTES] = current + minutes
        }
    }

    suspend fun addCategoryPlayed(category: String) {
        context.dataStore.edit { preferences ->
            val currentCategories = preferences[PreferencesKeys.CATEGORIES_PLAYED] ?: emptySet()
            preferences[PreferencesKeys.CATEGORIES_PLAYED] = currentCategories + category
        }
    }

    suspend fun resetDailyTasks() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_TASKS_COMPLETED] = 0
        }
    }

    suspend fun completeQuizLevel(level: Int) {
        context.dataStore.edit { preferences ->
            val completedLevels = preferences[PreferencesKeys.COMPLETED_QUIZ_LEVELS] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_QUIZ_LEVELS] = completedLevels + level.toString()
        }
    }

    fun getDailyTasksProgress(): Flow<Map<String, Int>> = context.dataStore.data.map { preferences ->
        mapOf(
            "quizzes_completed" to (preferences[PreferencesKeys.TOTAL_QUIZZES_COMPLETED] ?: 0),
            "correct_answers" to (preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0),
            "play_time_minutes" to (preferences[PreferencesKeys.PLAY_TIME_MINUTES] ?: 0),
            "categories_played" to (preferences[PreferencesKeys.CATEGORIES_PLAYED]?.size ?: 0),
            "coins_earned" to (preferences[PreferencesKeys.COINS] ?: 0),
            "quiz_levels_completed" to (preferences[PreferencesKeys.COMPLETED_QUIZ_LEVELS]?.size ?: 0)
        )
    }

    suspend fun shouldResetDailyTasks(): Boolean {
        val lastPlayDate = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_PLAY_DATE]?.let { LocalDate.parse(it) }
        }
        val today = LocalDate.now()
        return lastPlayDate.map { it != today }.firstOrNull() ?: true
    }

    fun isQuizLevelUnlocked(level: Int): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val unlockedLevels = preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] ?: 1
        level <= unlockedLevels
    }

    fun isQuizLevelCompleted(level: Int): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val completedLevels = preferences[PreferencesKeys.COMPLETED_QUIZ_LEVELS] ?: emptySet()
        level.toString() in completedLevels
    }
} 