package ru.plumsoftware.game.data

import java.time.LocalDate

/** Задание дня (ТЗ §6.7). */
enum class DailyQuestType(
    val id: String,
    val title: String,
    val target: Int,
    val reward: Int,
    val emoji: String,
    /**
     * Валюта награды. Кристаллы дают только «трудные» задания и понемногу:
     * в среднем ~1,25 💎 в день, т.е. заморозка серии (50 💎) — это больше месяца заданий.
     */
    val currency: RewardCurrency = RewardCurrency.COINS
) {
    CORRECT_10("correct_10", "Ответь правильно на 10 вопросов", 10, 50, "🎯"),
    THREE_STARS_2("three_stars_2", "Пройди 2 уровня на 3 звезды", 2, 3, "⭐", RewardCurrency.GEMS),
    NEW_TOPIC("new_topic", "Сыграй в новой теме", 1, 2, "🧭", RewardCurrency.GEMS),
    NO_HINTS("no_hints", "Пройди уровень без подсказок", 1, 50, "💪");

    companion object {
        fun fromId(id: String): DailyQuestType? = entries.find { it.id == id }
    }
}

/** Итог одного уровня — для заданий дня и достижений. */
data class LevelOutcome(
    val topicId: String,
    val difficulty: Int,
    val level: Int,
    val boss: Boolean,
    val correct: Int,
    val total: Int,
    val stars: Int,
    val usedHints: Boolean,
    /** В этой теме ещё не было пройденных уровней ни на одной сложности. */
    val newTopic: Boolean,
    /** Был ответ быстрее 3 секунд. */
    val fastAnswer: Boolean,
    val mistakes: Int
)

object DailyQuests {
    /** Детерминированный выбор задания по дате. */
    fun questFor(date: LocalDate): DailyQuestType {
        val day = date.toEpochDay()
        // Небольшое перемешивание, чтобы задания не шли строго по кругу.
        val mixed = (day * 7 + day / 4 * 3)
        val idx = Math.floorMod(mixed, DailyQuestType.entries.size.toLong()).toInt()
        return DailyQuestType.entries[idx]
    }

    /** На сколько продвигает задание один сыгранный уровень. */
    fun progressDelta(type: DailyQuestType, outcome: LevelOutcome): Int = when (type) {
        DailyQuestType.CORRECT_10 -> outcome.correct
        DailyQuestType.THREE_STARS_2 -> if (outcome.stars == 3) 1 else 0
        DailyQuestType.NEW_TOPIC -> if (outcome.newTopic && outcome.stars >= 1) 1 else 0
        DailyQuestType.NO_HINTS -> if (!outcome.usedHints && outcome.stars >= 1) 1 else 0
    }
}
