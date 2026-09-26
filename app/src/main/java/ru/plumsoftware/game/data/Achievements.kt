package ru.plumsoftware.game.data

enum class RewardCurrency(val emoji: String) { COINS("🪙"), GEMS("💎") }

/** Описание достижения (ТЗ §5.9). [iconKey] — имя файла из icons/emoji. */
data class AchievementDef(
    val id: String,
    val emoji: String,
    val iconKey: String,
    val title: String,
    val condition: String,
    val reward: Int,
    val currency: RewardCurrency
)

data class AchievementProgress(
    val def: AchievementDef,
    val current: Int,
    val target: Int,
    /** Дата получения ISO, если открыто. */
    val unlockedAt: String?
) {
    val reached: Boolean get() = current >= target
    val unlocked: Boolean get() = unlockedAt != null || reached
    val fraction: Float get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

object Achievements {
    val ALL: List<AchievementDef> = listOf(
        AchievementDef("first_step", "🎓", "ach_first_step", "Первый шаг", "Пройди 1 уровень", 20, RewardCurrency.COINS),
        AchievementDef("on_fire", "🔥", "streak_fire", "В огне", "Серия 3 дня", 50, RewardCurrency.COINS),
        AchievementDef("brain", "🧠", "ach_brain", "Умник", "100 верных ответов", 100, RewardCurrency.COINS),
        AchievementDef("lightning", "⚡", "ach_lightning", "Молния", "Ответ быстрее 3 сек", 30, RewardCurrency.COINS),
        AchievementDef("zoologist", "🦁", "topic_animals", "Зоолог", "Тема «Животные» 50 %", 100, RewardCurrency.COINS),
        AchievementDef("perfect", "💯", "ach_perfect", "Идеально", "5 из 5 без ошибок", 50, RewardCurrency.COINS),
        AchievementDef("treasure", "🎁", "map_chest", "Кладоискатель", "Открой 3 сундука", 50, RewardCurrency.COINS),
        AchievementDef("astronaut", "🚀", "topic_space", "Астронавт", "Пройди тему «Космос»", 10, RewardCurrency.GEMS),
        AchievementDef("champion", "👑", "map_boss", "Чемпион", "Победи 5 боссов", 10, RewardCurrency.GEMS),
        AchievementDef("marathon", "📅", "ach_marathon", "Марафон", "Серия 30 дней", 20, RewardCurrency.GEMS),
        AchievementDef("traveler", "🌍", "topic_countries", "Путешественник", "Начни все темы", 100, RewardCurrency.COINS),
        AchievementDef("rich", "💎", "currency_gem", "Богач", "Собери 5000 монет (за всё время)", 10, RewardCurrency.GEMS),
    )

    fun byId(id: String): AchievementDef? = ALL.find { it.id == id }

    /**
     * Прогресс всех достижений.
     * @param playableTopics темы, для которых есть вопросы (для «Путешественника»).
     */
    fun evaluate(state: GameState, playableTopics: Set<String>): List<AchievementProgress> {
        val half = LevelMap.LEVELS_PER_TOPIC / 2
        val topicsTarget = playableTopics.size.coerceAtLeast(1)
        val started = state.topicsStarted().count { it in playableTopics }
        return ALL.map { def ->
            val (current, target) = when (def.id) {
                "first_step" -> state.levelsPassed to 1
                "on_fire" -> maxOf(state.bestStreak, state.streakDays) to 3
                "brain" -> state.answersCorrect to 100
                "lightning" -> (if (state.fastAnswer) 1 else 0) to 1
                "zoologist" -> state.topicBestPassed("animals") to half
                "perfect" -> state.perfectLevels to 1
                "treasure" -> state.chestsOpened to 3
                "astronaut" -> state.topicBestPassed("space") to LevelMap.LEVELS_PER_TOPIC
                "champion" -> state.bossesBeaten to 5
                "marathon" -> maxOf(state.bestStreak, state.streakDays) to 30
                "traveler" -> started to topicsTarget
                "rich" -> state.lifetimeCoins to 5000
                else -> 0 to 1
            }
            AchievementProgress(def, current.coerceAtMost(target), target, state.unlockedAchievements[def.id])
        }
    }

    /** Достижения, которые выполнены, но ещё не выданы. */
    fun newlyReached(state: GameState, playableTopics: Set<String>): List<AchievementProgress> =
        evaluate(state, playableTopics).filter { it.reached && it.def.id !in state.unlockedAchievements }
}
