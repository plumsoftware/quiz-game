package ru.plumsoftware.game.data

import java.time.LocalDate

/** Настройки (ТЗ §2.1 settings, §5.11). */
data class GameSettings(
    val sound: Boolean = true,
    val music: Boolean = true,
    val vibro: Boolean = true,
    val voice: Boolean = false,
    val notifications: Boolean = true,
    val parentControl: Boolean = false,
    /** 0 — без лимита, иначе 15 / 30 / 60 минут. */
    val dailyLimitMin: Int = 0,
    val defaultDifficulty: Int = 0,
    val language: String = "ru"
)

/** Снимок всех данных игрока (ТЗ §2.1). */
data class GameState(
    /** false, пока DataStore не отдал первое значение. */
    val loaded: Boolean = false,

    // profile
    val playerName: String = "Игрок",
    val avatarId: String = "fox",
    val ageGroup: Int = 0,
    val profileCreated: Boolean = false,
    val createdAt: Long = 0L,

    // wallet
    val coins: Int = 0,
    val gems: Int = 0,

    // inventory
    val powerUpInventory: Map<String, Int> = emptyMap(),
    val streakFreezes: Int = 0,
    val ownedAvatars: Set<String> = Economy.START_AVATARS,

    // progress
    val currentTopicId: String = "animals",
    val currentDifficulty: Int = 0,
    val levelStars: Map<String, Int> = emptyMap(),
    val openedChests: Set<String> = emptySet(),
    val topicsPlayed: Set<String> = emptySet(),
    val totalXp: Int = 0,

    // stats
    val gamesPlayed: Int = 0,
    val answersTotal: Int = 0,
    val answersCorrect: Int = 0,
    /** Дата ISO → сколько вопросов отвечено в этот день. */
    val dailyAnswers: Map<String, Int> = emptyMap(),
    val topicCorrect: Map<String, Int> = emptyMap(),
    val topicTotal: Map<String, Int> = emptyMap(),

    // streak
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val lastPlayedDate: LocalDate? = null,
    val playedDates: Set<LocalDate> = emptySet(),
    val frozenDates: Set<LocalDate> = emptySet(),
    val streakRewardsClaimed: Set<String> = emptySet(),
    val goldenFrame: Boolean = false,

    // achievements
    /** id → дата получения ISO. */
    val unlockedAchievements: Map<String, String> = emptyMap(),
    val chestsOpened: Int = 0,
    val perfectLevels: Int = 0,
    val fastAnswer: Boolean = false,
    val lifetimeCoins: Int = 0,

    // daily quest
    val questDate: String = "",
    val questProgress: Int = 0,
    val questClaimed: Boolean = false,

    // settings / purchases / ads
    val settings: GameSettings = GameSettings(),
    val adsRemoved: Boolean = false,
    val freeCoinsClaimedToday: Int = 0,

    // time limit (§9.2)
    val playSecondsToday: Int = 0,
    val extraMinutesToday: Int = 0
) {
    val levelProgress: Economy.LevelProgress get() = Economy.levelProgress(totalXp)
    val level: Int get() = levelProgress.level
    val starsTotal: Int get() = levelStars.values.sum()
    val levelsPassed: Int get() = levelStars.count { it.value >= 1 }

    /** Побеждённые боссы — уровни 10/20/30 с хотя бы одной звездой. */
    val bossesBeaten: Int
        get() = levelStars.count { (key, stars) ->
            stars >= 1 && key.substringAfterLast('_').toIntOrNull()?.let { LevelMap.isBossLevel(it) } == true
        }

    fun hint(id: String): Int = powerUpInventory[id] ?: 0

    /** Лучший прогресс по теме среди трёх сложностей (пройдено уровней). */
    fun topicBestPassed(topicId: String): Int =
        (0..2).maxOf { d -> LevelMap.countPassed(topicId, d, levelStars) }

    /** Темы, в которых пройден хотя бы один уровень. */
    fun topicsStarted(): Set<String> =
        levelStars.filter { it.value >= 1 }.keys.map { it.substringBefore('_') }.toSet()

    fun todayQuest(today: LocalDate): DailyQuestType = DailyQuests.questFor(today)
    fun questProgressFor(today: LocalDate): Int = if (questDate == today.toString()) questProgress else 0
    fun questClaimedFor(today: LocalDate): Boolean = questDate == today.toString() && questClaimed

    /** Минут сыграно сегодня (для лимита). */
    fun playMinutesToday(): Int = playSecondsToday / 60

    /** Лимит на сегодня исчерпан (§9.2). Работает при включённом родительском контроле. */
    fun timeLimitReached(): Boolean {
        if (!settings.parentControl) return false
        val limit = settings.dailyLimitMin
        if (limit <= 0) return false
        return playSecondsToday >= (limit + extraMinutesToday) * 60
    }

    /** Кол-во вопросов по дням текущей недели Пн–Вс. */
    fun weekAnswers(today: LocalDate): List<Int> {
        val monday = today.with(java.time.DayOfWeek.MONDAY)
        return (0L until 7L).map { dailyAnswers[monday.plusDays(it).toString()] ?: 0 }
    }

    fun streakState(): StreakState = StreakState(
        current = streakDays,
        best = bestStreak,
        lastPlayed = lastPlayedDate,
        freezes = streakFreezes,
        frozenDates = frozenDates,
        playedDates = playedDates
    )
}
