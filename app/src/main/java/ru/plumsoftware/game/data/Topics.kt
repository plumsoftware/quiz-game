package ru.plumsoftware.game.data

/** Тема викторины (ТЗ §5.4, §12). id совпадает с ключами TopicPalette и icons/emoji/topic_*. */
data class Topic(
    val id: String,
    val emoji: String,
    val name: String
)

/** 10 тем по ТЗ. */
val ALL_TOPICS = listOf(
    Topic("animals", "🦁", "Животные"),
    Topic("space", "🚀", "Космос"),
    Topic("countries", "🌍", "Страны"),
    Topic("math", "🔢", "Математика"),
    Topic("nature", "🌿", "Природа"),
    Topic("science", "🔬", "Наука"),
    Topic("fairytales", "🧚", "Сказки"),
    Topic("music", "🎵", "Музыка"),
    Topic("sport", "⚽", "Спорт"),
    Topic("food", "🍎", "Еда"),
)

fun topicById(id: String): Topic = ALL_TOPICS.find { it.id == id } ?: ALL_TOPICS.first()

/**
 * Структура контента (ТЗ §6.1): 30 уровней = 3 секции по 10.
 * Секция на карте = 12 узлов: уровни 1–4, 🎁, 5–8, 🎁, 9, 👑 (босс = уровень 10).
 */
object LevelMap {
    const val LEVELS_PER_TOPIC = 30
    const val LEVELS_PER_SECTION = 10
    const val SECTIONS = LEVELS_PER_TOPIC / LEVELS_PER_SECTION

    enum class NodeType { LEVEL, CHEST, BOSS }
    enum class NodeState { PASSED, CURRENT, AVAILABLE, LOCKED }

    data class Node(
        val type: NodeType,
        val level: Int,        // номер уровня (для BOSS — уровень секции, кратный 10); для CHEST — 0
        val chestId: Int,      // порядковый номер сундука (для CHEST), иначе -1
        val stars: Int,        // заработанные звёзды (для LEVEL/BOSS)
        val state: NodeState,
        val xOffset: Int       // горизонтальное смещение змейки
    )

    private val xPattern = intArrayOf(0, 55, 85, 55, 0, -55, -85, -55)

    /** Ключ прогресса конкретного уровня. */
    fun levelKey(topicId: String, difficulty: Int, level: Int) = "${topicId}_${difficulty}_$level"

    /** Ключ сундука. */
    fun chestKey(topicId: String, difficulty: Int, chestId: Int) = "${topicId}_${difficulty}_c$chestId"

    /** Номер текущего уровня — первый непройденный (ТЗ §6.1). */
    fun currentLevel(topicId: String, difficulty: Int, levelStars: Map<String, Int>): Int {
        for (lvl in 1..LEVELS_PER_TOPIC) {
            if ((levelStars[levelKey(topicId, difficulty, lvl)] ?: 0) < 1) return lvl
        }
        return LEVELS_PER_TOPIC // всё пройдено
    }

    /**
     * Строит список узлов карты для пары (тема, сложность).
     * @param openedChests множество ключей открытых сундуков.
     */
    fun build(
        topicId: String,
        difficulty: Int,
        levelStars: Map<String, Int>,
        openedChests: Set<String>
    ): List<Node> {
        val current = currentLevel(topicId, difficulty, levelStars)
        val nodes = mutableListOf<Node>()
        var nodeIndex = 0
        var chestCounter = 0

        fun stateFor(level: Int): NodeState = when {
            (levelStars[levelKey(topicId, difficulty, level)] ?: 0) >= 1 -> NodeState.PASSED
            level == current -> NodeState.CURRENT
            else -> NodeState.LOCKED
        }

        fun addLevel(level: Int, boss: Boolean) {
            nodes += Node(
                type = if (boss) NodeType.BOSS else NodeType.LEVEL,
                level = level,
                chestId = -1,
                stars = levelStars[levelKey(topicId, difficulty, level)] ?: 0,
                state = stateFor(level),
                xOffset = xPattern[nodeIndex % xPattern.size]
            )
            nodeIndex++
        }

        fun addChest(afterLevel: Int) {
            val id = chestCounter++
            val precedingPassed = (levelStars[levelKey(topicId, difficulty, afterLevel)] ?: 0) >= 1
            val opened = chestKey(topicId, difficulty, id) in openedChests
            val state = when {
                opened -> NodeState.PASSED
                precedingPassed -> NodeState.AVAILABLE
                else -> NodeState.LOCKED
            }
            nodes += Node(NodeType.CHEST, 0, id, 0, state, xPattern[nodeIndex % xPattern.size])
            nodeIndex++
        }

        for (section in 0 until SECTIONS) {
            val base = section * LEVELS_PER_SECTION
            (1..4).forEach { addLevel(base + it, boss = false) }
            addChest(base + 4)
            (5..8).forEach { addLevel(base + it, boss = false) }
            addChest(base + 8)
            addLevel(base + 9, boss = false)
            addLevel(base + 10, boss = true)
        }
        return nodes
    }

    /** Звёзды за обычный уровень (5 вопросов) и босса (10 вопросов) — ТЗ §6.3. */
    fun starsForResult(correct: Int, total: Int, boss: Boolean): Int = if (boss) {
        when {
            correct >= 9 -> 3
            correct in 6..8 -> 2
            correct in 4..5 -> 1
            else -> 0
        }
    } else {
        when {
            correct >= 5 -> 3
            correct in 3..4 -> 2
            correct in 1..2 -> 1
            else -> 0
        }
    }

    fun isBossLevel(level: Int): Boolean = level % LEVELS_PER_SECTION == 0

    /** Сколько уровней пройдено (звёзд ≥ 1) для пары (тема, сложность). */
    fun countPassed(topicId: String, difficulty: Int, levelStars: Map<String, Int>): Int =
        (1..LEVELS_PER_TOPIC).count { (levelStars[levelKey(topicId, difficulty, it)] ?: 0) >= 1 }
}
