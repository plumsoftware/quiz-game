package ru.plumsoftware.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.plumsoftware.game.data.Achievements
import ru.plumsoftware.game.data.ChestReward
import ru.plumsoftware.game.data.DailyQuestType
import ru.plumsoftware.game.data.DailyQuests
import ru.plumsoftware.game.data.Economy
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameRules
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.HintIds
import ru.plumsoftware.game.data.LevelOutcome
import ru.plumsoftware.game.data.LevelSelector
import ru.plumsoftware.game.data.MathGenerator
import ru.plumsoftware.game.data.NameFilter
import ru.plumsoftware.game.data.QuestionParser
import ru.plumsoftware.game.data.QuizQuestion
import ru.plumsoftware.game.data.SaveCodec
import ru.plumsoftware.game.data.StreakLogic
import ru.plumsoftware.game.data.StreakMilestone
import ru.plumsoftware.game.data.StreakState
import java.time.LocalDate
import kotlin.random.Random

/** Проверки правил из ТЗ §6 (запуск: ./gradlew test). */
class GameRulesTest {

    private val today = LocalDate.of(2026, 9, 26)

    @Test
    fun xpCurveMatchesSpec() {
        assertEquals(100, Economy.xpToNext(1))
        assertEquals(400, Economy.xpToNext(7))
        assertEquals(2, Economy.levelForXp(100))
        assertEquals(3, Economy.levelForXp(250))
    }

    @Test
    fun levelRewards() {
        val medium = Economy.levelReward(5, 0, 5, GameDifficulty.MEDIUM, boss = false, previouslyPassed = false)
        assertEquals(3, medium.stars)
        assertEquals(5 * 15 + 20, medium.coins)
        assertEquals(40, medium.xp)

        val boss = Economy.levelReward(9, 0, 10, GameDifficulty.EASY, boss = true, previouslyPassed = false)
        assertEquals(3, boss.stars)
        assertEquals(90 + 20 + 100, boss.coins)
        assertEquals(5, boss.gems)

        val repeat = Economy.levelReward(5, 0, 5, GameDifficulty.EASY, boss = false, previouslyPassed = true)
        assertEquals(35, repeat.coins)
    }

    @Test
    fun starsTable() {
        val stars = (0..5).map { Economy.levelReward(it, 0, 5, GameDifficulty.EASY, false, false).stars }
        assertEquals(listOf(0, 1, 1, 2, 2, 3), stars)
        val bossStars = listOf(3, 4, 5, 6, 8, 9, 10).map { Economy.levelReward(it, 0, 10, GameDifficulty.EASY, true, false).stars }
        assertEquals(listOf(0, 1, 1, 2, 2, 3, 3), bossStars)
    }

    @Test
    fun starterKitGivenOnce() {
        var s = GameRules.createProfile(GameState(), "  Маша ", "frog", 1, 0)
        assertEquals(300, s.coins)
        assertEquals(5, s.gems)
        assertEquals(2, s.hint(HintIds.FIFTY))
        assertEquals(1, s.streakFreezes)
        assertTrue("frog" in s.ownedAvatars)
        assertEquals("Маша", s.playerName)
        assertEquals(GameDifficulty.MEDIUM.id, s.currentDifficulty)
        s = GameRules.createProfile(s, "Маша", "fox", 1, 0)
        assertEquals(300, s.coins)
    }

    @Test
    fun streakWithFreezeAndClockBack() {
        val d0 = LocalDate.of(2026, 9, 1)
        var s = StreakLogic.onLevelPassed(StreakState(), d0).first
        s = StreakLogic.onLevelPassed(s, d0.plusDays(1)).first
        assertEquals(2, s.current)
        s = StreakLogic.onLevelPassed(s, d0).first
        assertEquals(2, s.current) // перевод часов назад
        s = StreakLogic.onLevelPassed(s.copy(freezes = 1), d0.plusDays(3)).first
        assertEquals(3, s.current)
        assertEquals(0, s.freezes)
        s = StreakLogic.onLevelPassed(s, d0.plusDays(6)).first
        assertEquals(1, s.current)
        assertEquals(3, s.best)
    }

    @Test
    fun streakMilestones() {
        assertEquals(listOf(StreakMilestone.D7), StreakLogic.newMilestones(7, setOf("3")))
        val s = GameRules.grantMilestone(GameState(), StreakMilestone.D14)
        assertTrue("unicorn" in s.ownedAvatars)
    }

    @Test
    fun applyLevelUpdatesEverything() {
        val start = GameRules.createProfile(GameState(), "Маша", "fox", 0, 0)
        val out = LevelOutcome("animals", 0, 1, false, 5, 5, 0, usedHints = false, newTopic = false, fastAnswer = true, mistakes = 0)
        val r = GameRules.applyLevel(start, out, 0, today)
        assertEquals(3, r.state.levelStars["animals_0_1"])
        assertEquals(1, r.state.streakDays)
        assertEquals(5, r.state.dailyAnswers[today.toString()])
        assertTrue(r.outcome.newTopic)
        val (withAch, unlocked) = GameRules.unlockAchievements(r.state, setOf("animals", "space", "countries", "math"), today)
        val ids = unlocked.map { it.def.id }
        assertTrue(ids.containsAll(listOf("first_step", "lightning", "perfect")))
        assertEquals(r.state.coins + 20 + 30 + 50, withAch.coins)
    }

    @Test
    fun dailyQuestPoolAndClaim() {
        val all = (0L..30L).map { DailyQuests.questFor(today.plusDays(it)) }.toSet()
        assertEquals(DailyQuestType.entries.toSet(), all)
        val quest = DailyQuests.questFor(today)
        val done = GameState(questDate = today.toString(), questProgress = quest.target)
        val claimed = GameRules.claimQuest(done, today)!!
        assertEquals(quest.reward, claimed.second)
        assertTrue(GameRules.claimQuest(claimed.first, today) == null)
    }

    @Test
    fun chestAndShop() {
        val s = GameState(coins = 100, gems = 60)
        val c = GameRules.openChest(s, "k", ChestReward.Gems(4))
        assertEquals(64, c.gems)
        assertEquals(64, GameRules.openChest(c, "k", ChestReward.Gems(4)).gems)
        assertEquals(1, GameRules.buyStreakFreeze(s)!!.streakFreezes)
        assertTrue(GameRules.buyHint(s, HintIds.FIFTY, 150) == null)
    }

    @Test
    fun questionsParseShuffleAndSelect() {
        val json = """[{"id":"a1","topic":"animals","difficulty":0,"level":1,"text":"Q?","options":["a","b","c","d"],"answer":1}]"""
        val q = QuestionParser.parse(json).single()
        assertEquals("b", q.shuffledOptions(Random(3)).correctText)

        val pool = (1..45).map { QuizQuestion("q$it", "animals", 0, 0, "t$it", null, listOf("a", "b"), 0, null) }
        val l1 = LevelSelector.select(pool, "animals", 0, 1)
        val l2 = LevelSelector.select(pool, "animals", 0, 2)
        assertEquals(5, l1.size)
        assertTrue(l1.intersect(l2.toSet()).isEmpty())
        assertEquals(10, LevelSelector.select(pool, "animals", 0, 10).size)
    }

    @Test
    fun mathGenerator() {
        for (d in 0..2) for (lvl in listOf(1, 15, 30)) {
            val list = MathGenerator.generate(d, lvl, 10, Random(lvl + d))
            assertEquals(10, list.size)
            list.forEach { assertEquals(4, it.options.toSet().size); assertTrue(it.answer in 0..3) }
        }
    }

    @Test
    fun namesAndCloudCodec() {
        assertTrue(NameFilter.isAllowed("Серёжа"))
        assertFalse(NameFilter.isAllowed("   "))
        assertFalse(NameFilter.isAllowed("Fuck"))
        val m = mapOf("experience" to 120, "name" to "Маша", "b" to true, "set" to setOf("x", "y"), "l" to 5L)
        assertEquals(m, SaveCodec.decode(SaveCodec.encode(m)))
    }

    @Test
    fun gemShopAndGemQuests() {
        val s = GameState(gems = 30)
        val pack = GameRules.buyGemItem(s, ru.plumsoftware.game.data.GemShopItem.HINT_PACK)!!
        assertEquals(0, pack.gems)
        assertEquals(2, pack.hint(HintIds.FIFTY))
        assertTrue(GameRules.buyGemItem(s, ru.plumsoftware.game.data.GemShopItem.STREAK_FREEZE) == null)
        // Задание на кристаллы начисляет кристаллы, а не монеты.
        val gemDay = (0L..10L).map { today.plusDays(it) }
            .first { DailyQuests.questFor(it).currency == ru.plumsoftware.game.data.RewardCurrency.GEMS }
        val quest = DailyQuests.questFor(gemDay)
        val done = GameState(questDate = gemDay.toString(), questProgress = quest.target)
        val claimed = GameRules.claimQuest(done, gemDay)!!.first
        assertEquals(quest.reward, claimed.gems)
        assertEquals(0, claimed.coins)
    }

    @Test
    fun twelveAchievements() {
        assertEquals(12, Achievements.ALL.size)
    }
}
