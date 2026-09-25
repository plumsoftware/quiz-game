package ru.plumsoftware.game.data

/** Персонаж-аватар (ТЗ §5.2, §5.7). id совпадает с icons/emoji/avatar_*. */
data class Avatar(
    val id: String,
    val emoji: String,
    val name: String,
    val price: Int // 0 — доступен бесплатно
)

/** Порядок и цены — по прототипу и ТЗ §5.7. */
val ALL_AVATARS = listOf(
    Avatar("fox", "🦊", "Лисёнок", 0),
    Avatar("panda", "🐼", "Панда", 0),
    Avatar("frog", "🐸", "Лягушонок", 300),
    Avatar("tiger", "🐯", "Тигрёнок", 400),
    Avatar("unicorn", "🦄", "Единорог", 500),
    Avatar("octopus", "🐙", "Осьминог", 800),
)

fun avatarById(id: String): Avatar = ALL_AVATARS.find { it.id == id } ?: ALL_AVATARS.first()

/** Возрастная группа определяет сложность по умолчанию (ТЗ §5.2, §6.2). */
enum class AgeGroup(val id: Int, val label: String, val difficulty: GameDifficulty) {
    YOUNGER(0, "5–7", GameDifficulty.EASY),
    MIDDLE(1, "8–10", GameDifficulty.MEDIUM),
    OLDER(2, "11+", GameDifficulty.HARD);

    companion object {
        fun fromId(id: Int): AgeGroup = entries.find { it.id == id } ?: YOUNGER
    }
}

/** Сложность викторины (ТЗ §6.2). */
enum class GameDifficulty(
    val id: Int,
    val label: String,
    val secondsPerQuestion: Int,
    val lives: Int,
    val coinMultiplier: Double
) {
    EASY(0, "Легко", 20, 3, 1.0),
    MEDIUM(1, "Средне", 15, 3, 1.5),
    HARD(2, "Сложно", 10, 2, 2.0);

    companion object {
        fun fromId(id: Int): GameDifficulty = entries.find { it.id == id } ?: EASY
    }
}
