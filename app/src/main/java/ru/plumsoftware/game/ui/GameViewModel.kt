package ru.plumsoftware.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.plumsoftware.game.data.AchievementProgress
import ru.plumsoftware.game.data.Avatar
import ru.plumsoftware.game.data.ChestReward
import ru.plumsoftware.game.data.Economy
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.GameRules
import ru.plumsoftware.game.data.GameSettings
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.GemShopItem
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.data.LevelOutcome
import ru.plumsoftware.game.data.NameFilter
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.QuestionRepository
import ru.plumsoftware.game.data.QuizQuestion
import ru.plumsoftware.game.data.StreakMilestone
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.notifications.NotificationScheduler
import java.time.LocalDate
import java.util.ArrayDeque

/** Экраны приложения. Порядок определяет направление анимации перехода. */
enum class GameScreen {
    SPLASH,
    WELCOME,
    SIGNUP,
    HOME,
    STREAK,
    QUIZ,
    TOPICS,
    SHOP,
    PROFILE,
    ACHIEVEMENTS,
    SETTINGS,
    PRIVACY
}

/** Запущенный уровень карты. [sessionId] меняется при каждом старте — экран викторины сбрасывает состояние. */
data class QuizSession(
    val sessionId: Long,
    val topicId: String,
    val difficulty: GameDifficulty,
    val level: Int,
    val boss: Boolean,
    val questions: List<QuizQuestion>
)

/** Итог прохождения уровня экраном викторины. */
data class QuizRunStats(
    val correct: Int,
    val total: Int,
    val skipped: Int,
    val mistakes: Int,
    val usedHints: Boolean,
    val fastAnswer: Boolean
)

/** Данные для экрана результата (§5.6). */
data class LevelResultUi(
    val topicId: String,
    val level: Int,
    val boss: Boolean,
    val stars: Int,
    val correct: Int,
    val total: Int,
    val coins: Int,
    val xp: Int,
    val gems: Int,
    val doubled: Boolean = false,
    val hasNextLevel: Boolean = true
)

/** Тост о достижении (§5.9). */
data class AchievementToast(
    val id: String,
    val emoji: String,
    val iconKey: String,
    val title: String,
    val description: String,
    val reward: Int,
    val rewardEmoji: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameManager = GameManager(application)
    private val questions = QuestionRepository(application)
    private val notificationScheduler = NotificationScheduler(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentScreen = MutableStateFlow(GameScreen.SPLASH)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    val playableTopics: Set<String> get() = questions.playableTopics

    private val _session = MutableStateFlow<QuizSession?>(null)
    val session: StateFlow<QuizSession?> = _session.asStateFlow()

    private val _result = MutableStateFlow<LevelResultUi?>(null)
    val result: StateFlow<LevelResultUi?> = _result.asStateFlow()

    /** Новый уровень игрока → окно «Новый уровень!» (§6.5). */
    private val _levelUp = MutableStateFlow<Int?>(null)
    val levelUp: StateFlow<Int?> = _levelUp.asStateFlow()

    private val _chestReward = MutableStateFlow<ChestReward?>(null)
    val chestReward: StateFlow<ChestReward?> = _chestReward.asStateFlow()

    private val _milestone = MutableStateFlow<StreakMilestone?>(null)
    val milestone: StateFlow<StreakMilestone?> = _milestone.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()


    private val _pendingAchievementToast = MutableStateFlow<AchievementToast?>(null)
    val pendingAchievementToast: StateFlow<AchievementToast?> = _pendingAchievementToast.asStateFlow()
    private val achievementQueue = ArrayDeque<AchievementToast>()
    private var showingAchievement = false

    private var sessionCounter = 0L
    private var toastCounter = 0L

    init {
        viewModelScope.launch {
            gameManager.gameState.collect { _gameState.value = it }
        }
        viewModelScope.launch {
            val state = gameManager.gameState.filter { it.loaded }.first()
            // Заморозки/сброс серии при запуске (§6.6).
            if (state.profileCreated) gameManager.update { GameRules.onAppOpen(it, LocalDate.now()) }
            _currentScreen.value = if (state.profileCreated) GameScreen.HOME else GameScreen.WELCOME
            if (state.profileCreated) notificationScheduler.setEnabled(state.settings.notifications)
        }
    }

    // ---------- навигация ----------

    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }

    /** Системная кнопка «Назад». Возвращает false, если нужно закрыть приложение. */
    fun navigateUp(): Boolean {
        if (_result.value != null) {
            backToMap(); return true
        }
        val parent = when (_currentScreen.value) {
            GameScreen.SIGNUP -> GameScreen.WELCOME
            GameScreen.STREAK, GameScreen.TOPICS, GameScreen.SHOP, GameScreen.PROFILE, GameScreen.QUIZ -> GameScreen.HOME
            GameScreen.ACHIEVEMENTS, GameScreen.SETTINGS -> GameScreen.PROFILE
            GameScreen.PRIVACY -> GameScreen.SETTINGS
            else -> null
        } ?: return false
        _currentScreen.value = parent
        return true
    }

    fun showToast(message: String) {
        val id = ++toastCounter
        _toast.value = message
        viewModelScope.launch {
            delay(2000)
            if (toastCounter == id) _toast.value = null
        }
    }

    // ---------- профиль ----------

    /** Создаёт профиль (§5.2). Возвращает false, если имя не прошло фильтр. */
    fun createProfile(name: String, avatarId: String, ageGroup: Int): Boolean {
        if (!NameFilter.isAllowed(name)) return false
        viewModelScope.launch {
            val s = gameManager.update { GameRules.createProfile(it, name, avatarId, ageGroup, System.currentTimeMillis()) }
            notificationScheduler.setEnabled(s.settings.notifications)
            _currentScreen.value = GameScreen.HOME
        }
        return true
    }

    fun setCurrentTopic(topicId: String) {
        if (!questions.hasContent(topicId)) {
            showToast("Скоро! Вопросы этой темы готовятся")
            return
        }
        viewModelScope.launch {
            gameManager.update { it.copy(currentTopicId = topicId) }
            _currentScreen.value = GameScreen.HOME
        }
    }

    fun setDifficulty(difficulty: Int) {
        viewModelScope.launch { gameManager.update { it.copy(currentDifficulty = difficulty) } }
    }

    // ---------- карта и викторина ----------

    /** Нажатие на узел карты (§5.3). */
    fun onMapLevelClick(node: LevelMap.Node) {
        when (node.state) {
            LevelMap.NodeState.CURRENT, LevelMap.NodeState.PASSED -> startLevel(node.level)
            else -> showToast("Сначала пройди предыдущие уровни")
        }
    }

    fun startLevel(level: Int) {
        val s = _gameState.value
        val topicId = s.currentTopicId
        val difficulty = s.currentDifficulty
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { questions.questionsForLevel(topicId, difficulty, level) }
            if (list.isEmpty()) {
                showToast("Вопросы этой темы скоро появятся")
                return@launch
            }
            _result.value = null
            _session.value = QuizSession(
                sessionId = ++sessionCounter,
                topicId = topicId,
                difficulty = GameDifficulty.fromId(difficulty),
                level = level,
                boss = LevelMap.isBossLevel(level),
                questions = list
            )
            _currentScreen.value = GameScreen.QUIZ
        }
    }

    fun exitQuiz() {
        // Сессию не очищаем: экран викторины должен доанимировать уход.
        _currentScreen.value = GameScreen.HOME
    }

    /** Уровень закончен — начисляем награды и показываем результат (§5.6, §6). */
    fun onLevelFinished(stats: QuizRunStats) {
        val session = _session.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val outcome = LevelOutcome(
                topicId = session.topicId,
                difficulty = session.difficulty.id,
                level = session.level,
                boss = session.boss,
                correct = stats.correct,
                total = stats.total,
                stars = 0,
                usedHints = stats.usedHints,
                newTopic = false,
                fastAnswer = stats.fastAnswer,
                mistakes = stats.mistakes
            )
            val applied = gameManager.updateWith { s ->
                val r = GameRules.applyLevel(s, outcome, stats.skipped, today)
                val (withAch, unlocked) = GameRules.unlockAchievements(r.state, playableTopics, today)
                withAch to (r to unlocked)
            } ?: return@launch
            val (levelResult, unlocked) = applied
            val reward = levelResult.reward
            _result.value = LevelResultUi(
                topicId = session.topicId,
                level = session.level,
                boss = session.boss,
                stars = reward.stars,
                correct = stats.correct,
                total = stats.total,
                coins = reward.coins,
                xp = reward.xp,
                gems = reward.gems,
                hasNextLevel = session.level < LevelMap.LEVELS_PER_TOPIC || !reward.passed
            )
            levelResult.newPlayerLevels.lastOrNull()?.let { _levelUp.value = it }
            levelResult.milestones.lastOrNull()?.let { _milestone.value = it }
            enqueueAchievements(unlocked)
            if (levelResult.questJustCompleted) showToast("Задание дня выполнено! Забери награду на главной")
        }
    }

    /** «Следующий уровень» — текущий уровень карты после прохождения. */
    fun nextLevel() {
        val s = _gameState.value
        val passed = LevelMap.countPassed(s.currentTopicId, s.currentDifficulty, s.levelStars)
        if (passed >= LevelMap.LEVELS_PER_TOPIC) {
            backToMap()
            showToast("Тема пройдена! Выбери новую 🎉")
            return
        }
        startLevel(LevelMap.currentLevel(s.currentTopicId, s.currentDifficulty, s.levelStars))
    }

    fun replayLevel() {
        val session = _session.value ?: return backToMap()
        startLevel(session.level)
    }

    fun backToMap() {
        _result.value = null
        _currentScreen.value = GameScreen.HOME
    }

    /** Вызывается ТОЛЬКО из колбэка onRewarded rewarded-рекламы (§8). */
    fun onDoubleRewardEarned() {
        val r = _result.value ?: return
        if (r.doubled || r.coins <= 0) return
        _result.value = r.copy(doubled = true)
        viewModelScope.launch { gameManager.update { GameRules.doubleReward(it, r.coins) } }
    }

    fun dismissLevelUp() {
        _levelUp.value = null
    }

    fun dismissMilestone() {
        _milestone.value = null
    }

    // ---------- сундуки ----------

    fun openChest(chestId: Int) {
        val s = _gameState.value
        val key = LevelMap.chestKey(s.currentTopicId, s.currentDifficulty, chestId)
        if (key in s.openedChests) return
        val reward = ChestReward.roll()
        viewModelScope.launch {
            val today = LocalDate.now()
            val unlocked = gameManager.updateWith { st ->
                val opened = GameRules.openChest(st, key, reward)
                GameRules.unlockAchievements(opened, playableTopics, today)
            }
            _chestReward.value = reward
            unlocked?.let { enqueueAchievements(it) }
        }
    }

    fun dismissChest() {
        _chestReward.value = null
    }

    // ---------- задание дня ----------

    fun claimDailyQuest() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val quest = ru.plumsoftware.game.data.DailyQuests.questFor(today)
            val reward = gameManager.updateWith { GameRules.claimQuest(it, today) }
            if (reward != null) showToast("+$reward ${quest.currency.emoji} за задание дня!")
        }
    }

    // ---------- магазин ----------

    fun buyHint(type: PowerUpType, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s -> GameRules.buyHint(s, type.id, type.price)?.let { it to true } } ?: false
            onResult(ok)
        }
    }

    /** Использование подсказки во время викторины. */
    fun consumeHint(type: PowerUpType, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s ->
                if (s.hint(type.id) > 0) GameRules.addHint(s, type.id, -1) to true else null
            } ?: false
            onResult(ok)
        }
    }

    /** Покупка за кристаллы. */
    fun buyGemItem(item: GemShopItem, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s -> GameRules.buyGemItem(s, item)?.let { it to true } } ?: false
            onResult(ok)
        }
    }

    fun buyStreakFreeze(onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s -> GameRules.buyStreakFreeze(s)?.let { it to true } } ?: false
            onResult(ok)
        }
    }

    fun buyOrSelectAvatar(avatar: Avatar, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s -> GameRules.buyOrSelectAvatar(s, avatar)?.let { it to true } } ?: false
            onResult(ok)
        }
    }

    /** Вызывается ТОЛЬКО из колбэка onRewarded (§8). */
    fun onFreeCoinsEarned() {
        viewModelScope.launch {
            val ok = gameManager.updateWith { s -> GameRules.claimFreeCoins(s)?.let { it to true } } ?: false
            showToast(if (ok) "+${Economy.FREE_COINS_REWARD} 🪙" else "На сегодня всё")
        }
    }

    // ---------- настройки ----------

    fun updateSettings(transform: (GameSettings) -> GameSettings) {
        viewModelScope.launch {
            val before = _gameState.value.settings
            val s = gameManager.update { st ->
                val next = transform(st.settings)
                val diffChanged = next.defaultDifficulty != st.settings.defaultDifficulty
                st.copy(
                    settings = next,
                    currentDifficulty = if (diffChanged) next.defaultDifficulty else st.currentDifficulty
                )
            }
            if (before.notifications != s.settings.notifications) notificationScheduler.setEnabled(s.settings.notifications)
        }
    }

    // ---------- лимит времени (§9.2) ----------

    fun addPlayTime(seconds: Int) {
        if (!_gameState.value.profileCreated) return
        viewModelScope.launch { gameManager.update { it.copy(playSecondsToday = it.playSecondsToday + seconds) } }
    }

    /** Продление после родительского барьера. */
    fun extendTimeLimit(minutes: Int = 15) {
        viewModelScope.launch {
            gameManager.update { s ->
                // Если лимит уже превышен, продлеваем от фактически сыгранного времени.
                val playedMin = s.playSecondsToday / 60
                val base = maxOf(0, playedMin - s.settings.dailyLimitMin)
                s.copy(extraMinutesToday = maxOf(s.extraMinutesToday, base) + minutes)
            }
        }
    }

    // ---------- достижения ----------

    private fun enqueueAchievements(list: List<AchievementProgress>) {
        list.forEach { a ->
            achievementQueue.addLast(
                AchievementToast(
                    id = a.def.id,
                    emoji = a.def.emoji,
                    iconKey = a.def.iconKey,
                    title = a.def.title,
                    description = a.def.condition,
                    reward = a.def.reward,
                    rewardEmoji = a.def.currency.emoji
                )
            )
        }
        if (!showingAchievement) showNextAchievement()
    }

    private fun showNextAchievement() {
        if (achievementQueue.isEmpty()) {
            showingAchievement = false
            return
        }
        showingAchievement = true
        _pendingAchievementToast.value = achievementQueue.removeFirst()
    }

    fun dismissAchievementToast() {
        viewModelScope.launch {
            _pendingAchievementToast.value = null
            delay(400)
            showNextAchievement()
        }
    }

    fun topicName(topicId: String): String = topicById(topicId).name
}
