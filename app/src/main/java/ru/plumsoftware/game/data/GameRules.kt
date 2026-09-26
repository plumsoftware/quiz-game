package ru.plumsoftware.game.data

import java.time.LocalDate

/**
 * Все правила изменения прогресса — чистые функции над [GameState].
 * GameManager только читает/пишет состояние, а решения принимаются здесь (их легко тестировать).
 */
object GameRules {

    // ---------- базовые операции ----------

    /** Начисляет монеты; положительные суммы идут в «монеты за всё время» (достижение «Богач»). */
    fun addCoins(s: GameState, amount: Int): GameState = s.copy(
        coins = (s.coins + amount).coerceAtLeast(0),
        lifetimeCoins = s.lifetimeCoins + maxOf(0, amount)
    )

    fun addGems(s: GameState, amount: Int): GameState = s.copy(gems = (s.gems + amount).coerceAtLeast(0))

    fun addHint(s: GameState, id: String, amount: Int = 1): GameState =
        s.copy(powerUpInventory = s.powerUpInventory + (id to (s.hint(id) + amount).coerceAtLeast(0)))

    /** Добавляет XP; за каждый новый уровень игрока — 50 🪙 × N (§6.5). */
    fun addXp(s: GameState, xp: Int): Pair<GameState, List<Int>> {
        val before = s.level
        var result = s.copy(totalXp = s.totalXp + maxOf(0, xp))
        val newLevels = ((before + 1)..result.level).toList()
        newLevels.forEach { result = addCoins(result, Economy.levelUpReward(it)) }
        return result to newLevels
    }

    // ---------- профиль ----------

    /** Создание профиля + стартовый набор (§5.2, §6.8). Набор выдаётся один раз. */
    fun createProfile(s: GameState, name: String, avatarId: String, ageGroup: Int, now: Long): GameState {
        val difficulty = AgeGroup.fromId(ageGroup).difficulty.id
        var r = s.copy(
            playerName = NameFilter.clean(name).ifEmpty { "Игрок" },
            avatarId = avatarId,
            ageGroup = ageGroup,
            currentDifficulty = difficulty,
            settings = s.settings.copy(defaultDifficulty = difficulty),
            // Выбранный при регистрации персонаж выдаётся бесплатно (§5.2).
            ownedAvatars = s.ownedAvatars + Economy.START_AVATARS + avatarId
        )
        if (!s.profileCreated) {
            r = r.copy(profileCreated = true, createdAt = now, coins = r.coins + Economy.START_COINS, gems = r.gems + Economy.START_GEMS,
                streakFreezes = r.streakFreezes + Economy.START_STREAK_FREEZE)
            r = addHint(r, HintIds.FIFTY, Economy.START_FIFTY)
            r = addHint(r, HintIds.FREEZE, Economy.START_FREEZE)
            r = addHint(r, HintIds.SKIP, Economy.START_SKIP)
        }
        return r
    }

    /** При запуске: автоматическая трата заморозок / сброс серии (§6.6). */
    fun onAppOpen(s: GameState, today: LocalDate): GameState = withStreak(s, StreakLogic.onAppOpen(s.streakState(), today))

    private fun withStreak(s: GameState, st: StreakState): GameState = s.copy(
        streakDays = st.current,
        bestStreak = maxOf(s.bestStreak, st.best),
        lastPlayedDate = st.lastPlayed,
        streakFreezes = st.freezes,
        frozenDates = st.frozenDates,
        playedDates = st.playedDates
    )

    // ---------- уровень ----------

    data class LevelResult(
        val state: GameState,
        val outcome: LevelOutcome,
        val reward: Economy.LevelReward,
        val newPlayerLevels: List<Int>,
        val milestones: List<StreakMilestone>,
        val questJustCompleted: Boolean,
        val previouslyPassed: Boolean
    )

    fun applyLevel(s0: GameState, outcomeIn: LevelOutcome, skipped: Int, today: LocalDate): LevelResult {
        val key = LevelMap.levelKey(outcomeIn.topicId, outcomeIn.difficulty, outcomeIn.level)
        val previouslyPassed = (s0.levelStars[key] ?: 0) >= 1
        val difficulty = GameDifficulty.fromId(outcomeIn.difficulty)
        val reward = Economy.levelReward(
            correct = outcomeIn.correct,
            skipped = skipped,
            total = outcomeIn.total,
            difficulty = difficulty,
            boss = outcomeIn.boss,
            previouslyPassed = previouslyPassed
        )
        val outcome = outcomeIn.copy(
            stars = reward.stars,
            newTopic = outcomeIn.topicId !in s0.topicsStarted()
        )
        val answered = (outcome.total - skipped).coerceAtLeast(0)
        val todayKey = today.toString()
        val minKeep = today.minusDays(14).toString()

        var s = s0.copy(
            gamesPlayed = s0.gamesPlayed + 1,
            answersTotal = s0.answersTotal + answered,
            answersCorrect = s0.answersCorrect + outcome.correct,
            dailyAnswers = (s0.dailyAnswers + (todayKey to (s0.dailyAnswers[todayKey] ?: 0) + answered))
                .filterKeys { it >= minKeep },
            topicCorrect = s0.topicCorrect + (outcome.topicId to (s0.topicCorrect[outcome.topicId] ?: 0) + outcome.correct),
            topicTotal = s0.topicTotal + (outcome.topicId to (s0.topicTotal[outcome.topicId] ?: 0) + answered),
            topicsPlayed = s0.topicsPlayed + outcome.topicId,
            fastAnswer = s0.fastAnswer || outcome.fastAnswer,
            perfectLevels = s0.perfectLevels +
                if (outcome.mistakes == 0 && outcome.correct >= 5 && skipped == 0) 1 else 0
        )
        if (reward.stars > (s.levelStars[key] ?: 0)) {
            s = s.copy(levelStars = s.levelStars + (key to reward.stars))
        }
        s = addCoins(s, reward.coins)
        s = addGems(s, reward.gems)

        // Серия засчитывается, если уровень пройден (§6.6).
        var milestones = emptyList<StreakMilestone>()
        if (reward.passed) {
            val (st, _) = StreakLogic.onLevelPassed(s.streakState(), today)
            s = withStreak(s, st)
            milestones = StreakLogic.newMilestones(s.streakDays, s.streakRewardsClaimed)
            milestones.forEach { s = grantMilestone(s, it) }
        }

        // Задание дня (§6.7).
        val quest = DailyQuests.questFor(today)
        val wasDone = s.questProgressFor(today) >= quest.target
        val progress = (s.questProgressFor(today) + DailyQuests.progressDelta(quest, outcome)).coerceAtMost(quest.target)
        s = s.copy(questDate = todayKey, questProgress = progress, questClaimed = s.questClaimedFor(today))
        val questJustCompleted = !wasDone && progress >= quest.target

        val (withXp, newLevels) = addXp(s, reward.xp)
        return LevelResult(withXp, outcome, reward, newLevels, milestones, questJustCompleted, previouslyPassed)
    }

    /** Двойная награда за просмотр рекламы на результате (§5.6, §8). */
    fun doubleReward(s: GameState, coins: Int): GameState = addCoins(s, coins)

    // ---------- серия ----------

    fun grantMilestone(s: GameState, m: StreakMilestone): GameState {
        var r = s.copy(streakRewardsClaimed = s.streakRewardsClaimed + m.key)
        r = when (m) {
            StreakMilestone.D3 -> addCoins(r, 100)
            StreakMilestone.D7 -> {
                var x = addHint(r, HintIds.FIFTY, 2)
                x = addHint(x, HintIds.FREEZE, 1)
                addHint(x, HintIds.SKIP, 1)
            }
            StreakMilestone.D14 ->
                if ("unicorn" in r.ownedAvatars) addCoins(r, 500) else r.copy(ownedAvatars = r.ownedAvatars + "unicorn")
            StreakMilestone.D30 -> addGems(r.copy(goldenFrame = true), 20)
        }
        return r
    }

    // ---------- карта ----------

    fun openChest(s: GameState, chestKey: String, reward: ChestReward): GameState {
        if (chestKey in s.openedChests) return s
        var r = s.copy(openedChests = s.openedChests + chestKey, chestsOpened = s.chestsOpened + 1)
        r = when (reward) {
            is ChestReward.Coins -> addCoins(r, reward.amount)
            is ChestReward.Gems -> addGems(r, reward.amount)
            is ChestReward.Hint -> addHint(r, reward.hintId, 1)
        }
        return r
    }

    // ---------- задание дня ----------

    /** Забрать награду за выполненное задание. null — нечего забирать. */
    fun claimQuest(s: GameState, today: LocalDate): Pair<GameState, Int>? {
        val quest = DailyQuests.questFor(today)
        if (s.questClaimedFor(today) || s.questProgressFor(today) < quest.target) return null
        val claimed = s.copy(questDate = today.toString(), questProgress = quest.target, questClaimed = true)
        val r = when (quest.currency) {
            RewardCurrency.COINS -> addCoins(claimed, quest.reward)
            RewardCurrency.GEMS -> addGems(claimed, quest.reward)
        }
        return r to quest.reward
    }

    // ---------- достижения ----------

    /** Выдаёт все выполненные достижения с наградами. Повторяет, пока появляются новые (награда может открыть «Богача»). */
    fun unlockAchievements(s: GameState, playable: Set<String>, today: LocalDate): Pair<GameState, List<AchievementProgress>> {
        var r = s
        val unlocked = ArrayList<AchievementProgress>()
        repeat(4) {
            val fresh = Achievements.newlyReached(r, playable)
            if (fresh.isEmpty()) return r to unlocked
            fresh.forEach { a ->
                r = r.copy(unlockedAchievements = r.unlockedAchievements + (a.def.id to today.toString()))
                r = when (a.def.currency) {
                    RewardCurrency.COINS -> addCoins(r, a.def.reward)
                    RewardCurrency.GEMS -> addGems(r, a.def.reward)
                }
                unlocked += a.copy(unlockedAt = today.toString())
            }
        }
        return r to unlocked
    }

    // ---------- магазин ----------

    fun buyHint(s: GameState, id: String, price: Int): GameState? =
        if (s.coins < price) null else addHint(s.copy(coins = s.coins - price), id, 1)

    /** Покупка товара за кристаллы (§5.7). null — не хватает кристаллов. */
    fun buyGemItem(s: GameState, item: GemShopItem): GameState? {
        if (s.gems < item.price) return null
        var r = s.copy(gems = s.gems - item.price)
        r = when (item) {
            GemShopItem.STREAK_FREEZE -> r.copy(streakFreezes = r.streakFreezes + 1)
            GemShopItem.HINT_PACK -> {
                var x = addHint(r, HintIds.FIFTY, 2)
                x = addHint(x, HintIds.FREEZE, 1)
                addHint(x, HintIds.SKIP, 1)
            }
            GemShopItem.LIFE_PACK -> addHint(r, HintIds.LIFE, 2)
            GemShopItem.COIN_BAG -> addCoins(r, GemShopItem.COIN_BAG_AMOUNT)
        }
        return r
    }

    fun buyStreakFreeze(s: GameState): GameState? =
        if (s.gems < Economy.STREAK_FREEZE_PRICE_GEMS) null
        else s.copy(gems = s.gems - Economy.STREAK_FREEZE_PRICE_GEMS, streakFreezes = s.streakFreezes + 1)

    /** Покупка персонажа сразу его выбирает; купленный — просто выбирается (§5.7). */
    fun buyOrSelectAvatar(s: GameState, avatar: Avatar): GameState? = when {
        avatar.id in s.ownedAvatars || avatar.price == 0 ->
            s.copy(avatarId = avatar.id, ownedAvatars = s.ownedAvatars + avatar.id)
        s.coins >= avatar.price ->
            s.copy(coins = s.coins - avatar.price, avatarId = avatar.id, ownedAvatars = s.ownedAvatars + avatar.id)
        else -> null
    }

    fun claimFreeCoins(s: GameState): GameState? =
        if (s.freeCoinsClaimedToday >= Economy.FREE_COINS_DAILY_LIMIT) null
        else addCoins(s.copy(freeCoinsClaimedToday = s.freeCoinsClaimedToday + 1), Economy.FREE_COINS_REWARD)
}
