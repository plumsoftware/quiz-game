package ru.plumsoftware.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val COMPLETED_QUIZZES = stringSetPreferencesKey("completed_quizzes")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val AVATAR_ID = stringPreferencesKey("avatar_id")
        val AGE_GROUP = intPreferencesKey("age_group")
        val PROFILE_CREATED = booleanPreferencesKey("profile_created")
        val CURRENT_TOPIC = stringPreferencesKey("current_topic")
        val CURRENT_DIFFICULTY = intPreferencesKey("current_difficulty")
        val LEVEL_STARS = stringPreferencesKey("level_stars_json")
        val OPENED_CHESTS = stringSetPreferencesKey("opened_chests")
        val GEMS = intPreferencesKey("gems")
        val OWNED_AVATARS = stringSetPreferencesKey("owned_avatars")
        val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val FREE_COINS_CLAIMED = intPreferencesKey("free_coins_claimed_today")
        val FREE_COINS_DATE = stringPreferencesKey("free_coins_date")
        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")

        fun powerUpKey(type: PowerUpType) = intPreferencesKey("power_up_${type.id}")
    }

    val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        GameState(
            playerName = preferences[PreferencesKeys.PLAYER_NAME] ?: "Игрок",
            avatarId = preferences[PreferencesKeys.AVATAR_ID] ?: "fox",
            ageGroup = preferences[PreferencesKeys.AGE_GROUP] ?: 0,
            profileCreated = preferences[PreferencesKeys.PROFILE_CREATED] ?: false,
            currentTopicId = preferences[PreferencesKeys.CURRENT_TOPIC] ?: "animals",
            currentDifficulty = preferences[PreferencesKeys.CURRENT_DIFFICULTY]
                ?: (preferences[PreferencesKeys.AGE_GROUP] ?: 0),
            levelStars = decodeLevelStars(preferences[PreferencesKeys.LEVEL_STARS]),
            openedChests = preferences[PreferencesKeys.OPENED_CHESTS] ?: emptySet(),
            gems = preferences[PreferencesKeys.GEMS] ?: 0,
            ownedAvatars = preferences[PreferencesKeys.OWNED_AVATARS] ?: setOf("fox", "panda"),
            adsRemoved = preferences[PreferencesKeys.ADS_REMOVED] ?: false,
            freeCoinsClaimedToday = if (preferences[PreferencesKeys.FREE_COINS_DATE] == LocalDate.now().toString())
                preferences[PreferencesKeys.FREE_COINS_CLAIMED] ?: 0 else 0,
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
            categoriesPlayed = preferences[PreferencesKeys.CATEGORIES_PLAYED] ?: emptySet(),
            powerUpInventory = PowerUpType.entries.associate { type ->
                type.id to (preferences[PreferencesKeys.powerUpKey(type)] ?: 0)
            }
        )
    }

    suspend fun purchasePowerUp(type: PowerUpType): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val coins = preferences[PreferencesKeys.COINS] ?: 0
            if (coins >= type.price) {
                preferences[PreferencesKeys.COINS] = coins - type.price
                val key = PreferencesKeys.powerUpKey(type)
                preferences[key] = (preferences[key] ?: 0) + 1
                success = true
            }
        }
        return success
    }

    suspend fun consumePowerUp(type: PowerUpType): Boolean {
        var consumed = false
        context.dataStore.edit { preferences ->
            val key = PreferencesKeys.powerUpKey(type)
            val count = preferences[key] ?: 0
            if (count > 0) {
                preferences[key] = count - 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun addPowerUp(type: PowerUpType, amount: Int = 1) {
        context.dataStore.edit { preferences ->
            val key = PreferencesKeys.powerUpKey(type)
            preferences[key] = (preferences[key] ?: 0) + amount
        }
    }

    suspend fun getUnlockedAchievements(): Set<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.UNLOCKED_ACHIEVEMENTS] ?: emptySet()
        }.firstOrNull() ?: emptySet()
    }

    suspend fun unlockAchievement(id: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.UNLOCKED_ACHIEVEMENTS] ?: emptySet()
            preferences[PreferencesKeys.UNLOCKED_ACHIEVEMENTS] = current + id
        }
    }

    /**
     * Создаёт профиль и выдаёт стартовый набор (ТЗ §6.8).
     * Идемпотентно: стартовый набор начисляется только один раз.
     */
    suspend fun createProfile(name: String, avatarId: String, ageGroup: Int) {
        val trimmed = name.trim().take(14)
        context.dataStore.edit { preferences ->
            val alreadyCreated = preferences[PreferencesKeys.PROFILE_CREATED] ?: false
            preferences[PreferencesKeys.PLAYER_NAME] = trimmed.ifEmpty { "Игрок" }
            preferences[PreferencesKeys.AVATAR_ID] = avatarId
            preferences[PreferencesKeys.AGE_GROUP] = ageGroup
            if (!alreadyCreated) {
                preferences[PreferencesKeys.PROFILE_CREATED] = true
                // Стартовый набор (§6.8): 300 монет, 5 кристаллов, базовые подсказки, лиса+панда.
                preferences[PreferencesKeys.COINS] = (preferences[PreferencesKeys.COINS] ?: 0) + 300
                preferences[PreferencesKeys.GEMS] = (preferences[PreferencesKeys.GEMS] ?: 0) + 5
                preferences[PreferencesKeys.OWNED_AVATARS] =
                    (preferences[PreferencesKeys.OWNED_AVATARS] ?: emptySet()) + setOf("fox", "panda")
                fun addPowerUp(type: PowerUpType, amount: Int) {
                    val key = PreferencesKeys.powerUpKey(type)
                    preferences[key] = (preferences[key] ?: 0) + amount
                }
                addPowerUp(PowerUpType.FIFTY_FIFTY, 2)
                addPowerUp(PowerUpType.FREEZE_TIME, 1)
                addPowerUp(PowerUpType.SKIP_QUESTION, 1)
            }
        }
    }

    suspend fun updatePlayerName(name: String) {
        val trimmed = name.trim().take(24)
        if (trimmed.isEmpty()) return
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_NAME] = trimmed
        }
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
        val newUnlockedLevels = GameData.unlockedQuizTiersForPlayerLevel(playerLevel)
        if (newUnlockedLevels > currentUnlockedLevels) {
            preferences[PreferencesKeys.UNLOCKED_QUIZ_LEVELS] = newUnlockedLevels
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

    suspend fun addGems(amount: Int) {
        context.dataStore.edit { it[PreferencesKeys.GEMS] = (it[PreferencesKeys.GEMS] ?: 0) + amount }
    }

    /** Покупка персонажа за монеты и его выбор (ТЗ §5.7). */
    suspend fun purchaseAvatar(avatarId: String, price: Int): Boolean {
        var ok = false
        context.dataStore.edit { preferences ->
            val owned = preferences[PreferencesKeys.OWNED_AVATARS] ?: emptySet()
            if (avatarId in owned) {
                preferences[PreferencesKeys.AVATAR_ID] = avatarId // уже куплен — просто выбрать
                ok = true
            } else {
                val coins = preferences[PreferencesKeys.COINS] ?: 0
                if (coins >= price) {
                    preferences[PreferencesKeys.COINS] = coins - price
                    preferences[PreferencesKeys.OWNED_AVATARS] = owned + avatarId
                    preferences[PreferencesKeys.AVATAR_ID] = avatarId
                    ok = true
                }
            }
        }
        return ok
    }

    /** Начисление «бесплатных монет» за рекламу с суточным лимитом (ТЗ §5.7, §8). */
    suspend fun claimFreeCoins(amount: Int, dailyLimit: Int): Boolean {
        var ok = false
        context.dataStore.edit { preferences ->
            val today = LocalDate.now().toString()
            val claimed = if (preferences[PreferencesKeys.FREE_COINS_DATE] == today)
                preferences[PreferencesKeys.FREE_COINS_CLAIMED] ?: 0 else 0
            if (claimed < dailyLimit) {
                preferences[PreferencesKeys.FREE_COINS_DATE] = today
                preferences[PreferencesKeys.FREE_COINS_CLAIMED] = claimed + 1
                preferences[PreferencesKeys.COINS] = (preferences[PreferencesKeys.COINS] ?: 0) + amount
                ok = true
            }
        }
        return ok
    }

    suspend fun setAdsRemoved(removed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ADS_REMOVED] = removed }
    }

    suspend fun setCurrentTopic(topicId: String) {
        context.dataStore.edit { it[PreferencesKeys.CURRENT_TOPIC] = topicId }
    }

    suspend fun setCurrentDifficulty(difficulty: Int) {
        context.dataStore.edit { it[PreferencesKeys.CURRENT_DIFFICULTY] = difficulty }
    }

    /** Записывает звёзды за уровень карты, сохраняя лучший результат (ТЗ §6.3). */
    suspend fun recordLevelStars(topicId: String, difficulty: Int, level: Int, stars: Int) {
        if (stars < 1) return
        context.dataStore.edit { preferences ->
            val map = decodeLevelStars(preferences[PreferencesKeys.LEVEL_STARS]).toMutableMap()
            val key = LevelMap.levelKey(topicId, difficulty, level)
            if (stars > (map[key] ?: 0)) {
                map[key] = stars
                preferences[PreferencesKeys.LEVEL_STARS] = encodeLevelStars(map)
            }
        }
    }

    suspend fun openChest(topicId: String, difficulty: Int, chestId: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.OPENED_CHESTS] ?: emptySet()
            preferences[PreferencesKeys.OPENED_CHESTS] =
                current + LevelMap.chestKey(topicId, difficulty, chestId)
        }
    }

    suspend fun completeQuiz(quizId: Int) {
        context.dataStore.edit { preferences ->
            val completedQuizzes = preferences[PreferencesKeys.COMPLETED_QUIZZES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_QUIZZES] = completedQuizzes + quizId.toString()
        }
    }

    fun getDailyTasksProgress(): Flow<Map<String, Int>> = context.dataStore.data.map { preferences ->
        mapOf(
            "quizzes_completed" to (preferences[PreferencesKeys.TOTAL_QUIZZES_COMPLETED] ?: 0),
            "correct_answers" to (preferences[PreferencesKeys.TOTAL_CORRECT_ANSWERS] ?: 0),
            "play_time_minutes" to (preferences[PreferencesKeys.PLAY_TIME_MINUTES] ?: 0),
            "categories_played" to (preferences[PreferencesKeys.CATEGORIES_PLAYED]?.size ?: 0),
            "coins_earned" to (preferences[PreferencesKeys.COINS] ?: 0),
            "quizzes_completed" to (preferences[PreferencesKeys.COMPLETED_QUIZZES]?.size ?: 0)
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

    fun isQuizCompleted(quizId: Int): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val completedQuizzes = preferences[PreferencesKeys.COMPLETED_QUIZZES] ?: emptySet()
        quizId.toString() in completedQuizzes
    }

    private fun encodeLevelStars(map: Map<String, Int>): String =
        Json.encodeToString(map)

    private fun decodeLevelStars(raw: String?): Map<String, Int> =
        if (raw.isNullOrEmpty()) emptyMap()
        else try {
            Json.decodeFromString<Map<String, Int>>(raw)
        } catch (e: Exception) {
            emptyMap()
        }
} 