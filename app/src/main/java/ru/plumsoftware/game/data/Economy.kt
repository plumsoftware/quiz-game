package ru.plumsoftware.game.data

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/** Идентификаторы подсказок в инвентаре (совпадают с PowerUpType.id). */
object HintIds {
    const val FIFTY = "fifty_fifty"
    const val FREEZE = "freeze_time"
    const val LIFE = "extra_life"
    const val SKIP = "skip_question"

    val ALL = listOf(FIFTY, FREEZE, LIFE, SKIP)
}

/** Экономика и прогрессия (ТЗ §6.3–6.5, §6.8, §5.7). */
object Economy {
    const val COINS_PER_CORRECT = 10
    const val XP_PER_CORRECT = 8
    const val THREE_STAR_BONUS = 20
    const val BOSS_COINS = 100
    const val BOSS_GEMS = 5

    const val FREE_COINS_REWARD = 100
    const val FREE_COINS_DAILY_LIMIT = 5

    const val STREAK_FREEZE_PRICE_GEMS = 50

    // Стартовый набор (§6.8)
    const val START_COINS = 300
    const val START_GEMS = 5
    const val START_FIFTY = 2
    const val START_FREEZE = 1
    const val START_SKIP = 1
    const val START_STREAK_FREEZE = 1
    val START_AVATARS = setOf("fox", "panda")

    /** XP для перехода с уровня [level] на следующий: 100 + 50 × (N − 1). */
    fun xpToNext(level: Int): Int = 100 + 50 * (level - 1)

    data class LevelProgress(val level: Int, val xpInLevel: Int, val xpNeeded: Int) {
        val fraction: Float get() = if (xpNeeded <= 0) 0f else xpInLevel.toFloat() / xpNeeded
    }

    fun levelProgress(totalXp: Int): LevelProgress {
        var level = 1
        var rest = max(0, totalXp)
        while (rest >= xpToNext(level)) {
            rest -= xpToNext(level)
            level++
        }
        return LevelProgress(level, rest, xpToNext(level))
    }

    fun levelForXp(totalXp: Int): Int = levelProgress(totalXp).level

    /** Награда за новый уровень игрока: 50 🪙 × N (§6.5). */
    fun levelUpReward(newLevel: Int): Int = 50 * newLevel

    /** Звания (§5.8). */
    fun rank(level: Int): String = when {
        level >= 15 -> "Профессор"
        level >= 10 -> "Мудрец"
        level >= 6 -> "Знаток"
        level >= 3 -> "Ученик"
        else -> "Новичок"
    }

    /** Монеты за один верный ответ с учётом множителя сложности (§6.2, §6.4). */
    fun coinsPerCorrect(difficulty: GameDifficulty): Int =
        (COINS_PER_CORRECT * difficulty.coinMultiplier).roundToInt()

    data class LevelReward(
        val stars: Int,
        val coins: Int,
        val xp: Int,
        val gems: Int,
        val bossBonus: Boolean,
        val threeStarBonus: Boolean
    ) {
        val passed: Boolean get() = stars >= 1
    }

    /**
     * Награда за уровень карты.
     * @param correct верные ответы;
     * @param skipped пропущенные подсказкой «Пропуск» — не дают награды, но и не портят звёзды;
     * @param previouslyPassed уровень уже был пройден раньше — монеты ×50 % (§6.3), бонус босса не повторяется.
     */
    fun levelReward(
        correct: Int,
        skipped: Int,
        total: Int,
        difficulty: GameDifficulty,
        boss: Boolean,
        previouslyPassed: Boolean
    ): LevelReward {
        val stars = LevelMap.starsForResult(correct + skipped, total, boss)
        var coins = correct * coinsPerCorrect(difficulty)
        val threeStar = stars == 3
        if (threeStar) coins += THREE_STAR_BONUS
        if (previouslyPassed) coins /= 2
        var gems = 0
        val bossBonus = boss && stars >= 1 && !previouslyPassed
        if (bossBonus) {
            coins += BOSS_COINS
            gems += BOSS_GEMS
        }
        return LevelReward(
            stars = stars,
            coins = coins,
            xp = correct * XP_PER_CORRECT,
            gems = gems,
            bossBonus = bossBonus,
            threeStarBonus = threeStar
        )
    }
}

/** Награда из сундука на карте (§6.4): 50–150 🪙 / 1 подсказка / 3–5 💎. */
sealed class ChestReward {
    data class Coins(val amount: Int) : ChestReward()
    data class Hint(val hintId: String) : ChestReward()
    data class Gems(val amount: Int) : ChestReward()

    companion object {
        fun roll(random: Random = Random.Default): ChestReward = when (random.nextInt(3)) {
            0 -> Coins(random.nextInt(50, 151))
            1 -> Hint(HintIds.ALL[random.nextInt(HintIds.ALL.size)])
            else -> Gems(random.nextInt(3, 6))
        }
    }
}
