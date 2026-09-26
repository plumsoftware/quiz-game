package ru.plumsoftware.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

/**
 * Хранилище прогресса (ТЗ §2.1) поверх Preferences DataStore.
 * Вся логика изменений — в [GameRules]; здесь только чтение/запись [GameState] целиком.
 */
class GameManager(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object K {
        // profile
        val NAME = stringPreferencesKey("player_name")
        val AVATAR = stringPreferencesKey("avatar_id")
        val AGE = intPreferencesKey("age_group")
        val PROFILE_CREATED = booleanPreferencesKey("profile_created")
        val CREATED_AT = longPreferencesKey("created_at")

        // wallet / inventory
        val COINS = intPreferencesKey("coins")
        val GEMS = intPreferencesKey("gems")
        val STREAK_FREEZES = intPreferencesKey("streak_freezes")
        val OWNED_AVATARS = stringSetPreferencesKey("owned_avatars")
        fun hint(id: String) = intPreferencesKey("power_up_$id")

        // progress
        val TOPIC = stringPreferencesKey("current_topic")
        val DIFFICULTY = intPreferencesKey("current_difficulty")
        val LEVEL_STARS = stringPreferencesKey("level_stars_json")
        val OPENED_CHESTS = stringSetPreferencesKey("opened_chests")
        val TOPICS_PLAYED = stringSetPreferencesKey("topics_played")
        val XP = intPreferencesKey(SaveCodec.XP_KEY) // "experience" — суммарный XP

        // stats
        val GAMES = intPreferencesKey("total_quizzes_completed")
        val ANSWERS_TOTAL = intPreferencesKey("total_answers")
        val ANSWERS_CORRECT = intPreferencesKey("total_correct_answers")
        val DAILY_ANSWERS = stringPreferencesKey("daily_answers_json")
        val TOPIC_CORRECT = stringPreferencesKey("topic_correct_json")
        val TOPIC_TOTAL = stringPreferencesKey("topic_total_json")

        // streak
        val STREAK = intPreferencesKey("streak_days")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val LAST_PLAYED = stringPreferencesKey("last_play_date")
        val PLAYED_DATES = stringSetPreferencesKey("played_dates")
        val FROZEN_DATES = stringSetPreferencesKey("frozen_dates")
        val STREAK_REWARDS = stringSetPreferencesKey("streak_rewards_claimed")
        val GOLDEN_FRAME = booleanPreferencesKey("golden_frame")

        // achievements
        val ACHIEVEMENTS = stringPreferencesKey("achievements_json")
        val CHESTS_OPENED = intPreferencesKey("chests_opened")
        val PERFECT_LEVELS = intPreferencesKey("perfect_levels")
        val FAST_ANSWER = booleanPreferencesKey("fast_answer")
        val LIFETIME_COINS = intPreferencesKey("lifetime_coins")

        // daily quest
        val QUEST_DATE = stringPreferencesKey("quest_date")
        val QUEST_PROGRESS = intPreferencesKey("quest_progress")
        val QUEST_CLAIMED = booleanPreferencesKey("quest_claimed")

        // settings
        val S_SOUND = booleanPreferencesKey("set_sound")
        val S_MUSIC = booleanPreferencesKey("set_music")
        val S_VIBRO = booleanPreferencesKey("set_vibro")
        val S_VOICE = booleanPreferencesKey("set_voice")
        val S_NOTIF = booleanPreferencesKey("set_notifications")
        val S_PARENT = booleanPreferencesKey("set_parent_control")
        val S_LIMIT = intPreferencesKey("set_daily_limit_min")
        val S_DIFF = intPreferencesKey("set_default_difficulty")
        val S_LANG = stringPreferencesKey("set_language")

        // purchases / ads / per-day counters
        val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val DAY_STAMP = stringPreferencesKey("day_stamp")
        val FREE_COINS = intPreferencesKey("free_coins_claimed_today")
        val PLAY_SECONDS = intPreferencesKey("play_seconds_today")
        val EXTRA_MINUTES = intPreferencesKey("extra_minutes_today")

        // служебное (не входит в GameState)
        val LAST_NOTIFICATION = stringPreferencesKey("last_notification_date")
    }

    val gameState: Flow<GameState> = dataStore.data.map { read(it) }

    suspend fun current(): GameState = gameState.first()

    /** Атомарно меняет состояние. */
    suspend fun update(transform: (GameState) -> GameState): GameState {
        var result = GameState()
        dataStore.edit { p ->
            val next = transform(read(p))
            write(p, next)
            result = next
        }
        return result
    }

    /** Атомарно меняет состояние и возвращает дополнительный результат. null из transform — ничего не менять. */
    suspend fun <R> updateWith(transform: (GameState) -> Pair<GameState, R>?): R? {
        var out: R? = null
        dataStore.edit { p ->
            val res = transform(read(p)) ?: return@edit
            write(p, res.first)
            out = res.second
        }
        return out
    }

    // ---------- уведомления ----------

    suspend fun lastNotificationDate(): String? = dataStore.data.first()[K.LAST_NOTIFICATION]

    suspend fun setLastNotificationDate(date: String) {
        dataStore.edit { it[K.LAST_NOTIFICATION] = date }
    }

    // ---------- облачная копия (§2.2) ----------

    /** Все значения хранилища как «ключ → значение» для облака. */
    suspend fun exportRaw(): Map<String, Any> =
        dataStore.data.first().asMap().mapKeys { it.key.name }.filterKeys { it != K.LAST_NOTIFICATION.name }

    /** Полностью заменяет локальные данные снимком из облака. */
    suspend fun importRaw(values: Map<String, Any>) {
        dataStore.edit { p ->
            p.clear()
            values.forEach { (name, v) ->
                when (v) {
                    is Int -> p[intPreferencesKey(name)] = v
                    is Long -> p[longPreferencesKey(name)] = v
                    is Boolean -> p[booleanPreferencesKey(name)] = v
                    is Float -> p[floatPreferencesKey(name)] = v
                    is Double -> p[doublePreferencesKey(name)] = v
                    is String -> p[stringPreferencesKey(name)] = v
                    is Set<*> -> p[stringSetPreferencesKey(name)] = v.filterIsInstance<String>().toSet()
                }
            }
        }
    }

    // ---------- чтение / запись ----------

    private fun read(p: Preferences): GameState {
        val today = LocalDate.now().toString()
        val sameDay = p[K.DAY_STAMP] == today
        val age = p[K.AGE] ?: 0
        val defaultDifficulty = p[K.S_DIFF] ?: AgeGroup.fromId(age).difficulty.id
        return GameState(
            loaded = true,
            playerName = p[K.NAME] ?: "Игрок",
            avatarId = p[K.AVATAR] ?: "fox",
            ageGroup = age,
            profileCreated = p[K.PROFILE_CREATED] ?: false,
            createdAt = p[K.CREATED_AT] ?: 0L,
            coins = p[K.COINS] ?: 0,
            gems = p[K.GEMS] ?: 0,
            powerUpInventory = HintIds.ALL.associateWith { p[K.hint(it)] ?: 0 },
            streakFreezes = p[K.STREAK_FREEZES] ?: 0,
            ownedAvatars = (p[K.OWNED_AVATARS] ?: emptySet()) + Economy.START_AVATARS,
            currentTopicId = p[K.TOPIC] ?: "animals",
            currentDifficulty = p[K.DIFFICULTY] ?: defaultDifficulty,
            levelStars = decodeIntMap(p[K.LEVEL_STARS]),
            openedChests = p[K.OPENED_CHESTS] ?: emptySet(),
            topicsPlayed = p[K.TOPICS_PLAYED] ?: emptySet(),
            totalXp = p[K.XP] ?: 0,
            gamesPlayed = p[K.GAMES] ?: 0,
            answersTotal = p[K.ANSWERS_TOTAL] ?: 0,
            answersCorrect = p[K.ANSWERS_CORRECT] ?: 0,
            dailyAnswers = decodeIntMap(p[K.DAILY_ANSWERS]),
            topicCorrect = decodeIntMap(p[K.TOPIC_CORRECT]),
            topicTotal = decodeIntMap(p[K.TOPIC_TOTAL]),
            streakDays = p[K.STREAK] ?: 0,
            bestStreak = p[K.BEST_STREAK] ?: 0,
            lastPlayedDate = p[K.LAST_PLAYED]?.let { parseDate(it) },
            playedDates = (p[K.PLAYED_DATES] ?: emptySet()).mapNotNull { parseDate(it) }.toSet(),
            frozenDates = (p[K.FROZEN_DATES] ?: emptySet()).mapNotNull { parseDate(it) }.toSet(),
            streakRewardsClaimed = p[K.STREAK_REWARDS] ?: emptySet(),
            goldenFrame = p[K.GOLDEN_FRAME] ?: false,
            unlockedAchievements = decodeStringMap(p[K.ACHIEVEMENTS]),
            chestsOpened = p[K.CHESTS_OPENED] ?: 0,
            perfectLevels = p[K.PERFECT_LEVELS] ?: 0,
            fastAnswer = p[K.FAST_ANSWER] ?: false,
            lifetimeCoins = p[K.LIFETIME_COINS] ?: 0,
            questDate = p[K.QUEST_DATE] ?: "",
            questProgress = p[K.QUEST_PROGRESS] ?: 0,
            questClaimed = p[K.QUEST_CLAIMED] ?: false,
            settings = GameSettings(
                sound = p[K.S_SOUND] ?: true,
                music = p[K.S_MUSIC] ?: true,
                vibro = p[K.S_VIBRO] ?: true,
                voice = p[K.S_VOICE] ?: false,
                notifications = p[K.S_NOTIF] ?: true,
                parentControl = p[K.S_PARENT] ?: false,
                dailyLimitMin = p[K.S_LIMIT] ?: 0,
                defaultDifficulty = defaultDifficulty,
                language = p[K.S_LANG] ?: "ru"
            ),
            adsRemoved = p[K.ADS_REMOVED] ?: false,
            freeCoinsClaimedToday = if (sameDay) p[K.FREE_COINS] ?: 0 else 0,
            playSecondsToday = if (sameDay) p[K.PLAY_SECONDS] ?: 0 else 0,
            extraMinutesToday = if (sameDay) p[K.EXTRA_MINUTES] ?: 0 else 0
        )
    }

    private fun write(p: MutablePreferences, s: GameState) {
        p[K.NAME] = s.playerName
        p[K.AVATAR] = s.avatarId
        p[K.AGE] = s.ageGroup
        p[K.PROFILE_CREATED] = s.profileCreated
        p[K.CREATED_AT] = s.createdAt
        p[K.COINS] = s.coins
        p[K.GEMS] = s.gems
        HintIds.ALL.forEach { p[K.hint(it)] = s.hint(it) }
        p[K.STREAK_FREEZES] = s.streakFreezes
        p[K.OWNED_AVATARS] = s.ownedAvatars
        p[K.TOPIC] = s.currentTopicId
        p[K.DIFFICULTY] = s.currentDifficulty
        p[K.LEVEL_STARS] = encodeIntMap(s.levelStars)
        p[K.OPENED_CHESTS] = s.openedChests
        p[K.TOPICS_PLAYED] = s.topicsPlayed
        p[K.XP] = s.totalXp
        p[K.GAMES] = s.gamesPlayed
        p[K.ANSWERS_TOTAL] = s.answersTotal
        p[K.ANSWERS_CORRECT] = s.answersCorrect
        p[K.DAILY_ANSWERS] = encodeIntMap(s.dailyAnswers)
        p[K.TOPIC_CORRECT] = encodeIntMap(s.topicCorrect)
        p[K.TOPIC_TOTAL] = encodeIntMap(s.topicTotal)
        p[K.STREAK] = s.streakDays
        p[K.BEST_STREAK] = s.bestStreak
        val last = s.lastPlayedDate
        if (last != null) p[K.LAST_PLAYED] = last.toString() else p.remove(K.LAST_PLAYED)
        p[K.PLAYED_DATES] = s.playedDates.map { it.toString() }.toSet()
        p[K.FROZEN_DATES] = s.frozenDates.map { it.toString() }.toSet()
        p[K.STREAK_REWARDS] = s.streakRewardsClaimed
        p[K.GOLDEN_FRAME] = s.goldenFrame
        p[K.ACHIEVEMENTS] = encodeStringMap(s.unlockedAchievements)
        p[K.CHESTS_OPENED] = s.chestsOpened
        p[K.PERFECT_LEVELS] = s.perfectLevels
        p[K.FAST_ANSWER] = s.fastAnswer
        p[K.LIFETIME_COINS] = s.lifetimeCoins
        p[K.QUEST_DATE] = s.questDate
        p[K.QUEST_PROGRESS] = s.questProgress
        p[K.QUEST_CLAIMED] = s.questClaimed
        p[K.S_SOUND] = s.settings.sound
        p[K.S_MUSIC] = s.settings.music
        p[K.S_VIBRO] = s.settings.vibro
        p[K.S_VOICE] = s.settings.voice
        p[K.S_NOTIF] = s.settings.notifications
        p[K.S_PARENT] = s.settings.parentControl
        p[K.S_LIMIT] = s.settings.dailyLimitMin
        p[K.S_DIFF] = s.settings.defaultDifficulty
        p[K.S_LANG] = s.settings.language
        p[K.ADS_REMOVED] = s.adsRemoved
        p[K.DAY_STAMP] = LocalDate.now().toString()
        p[K.FREE_COINS] = s.freeCoinsClaimedToday
        p[K.PLAY_SECONDS] = s.playSecondsToday
        p[K.EXTRA_MINUTES] = s.extraMinutesToday
    }

    private fun parseDate(raw: String): LocalDate? = try {
        LocalDate.parse(raw)
    } catch (e: Exception) {
        null
    }

    private fun encodeIntMap(map: Map<String, Int>): String =
        JsonObject(map.mapValues { JsonPrimitive(it.value) }).toString()

    private fun decodeIntMap(raw: String?): Map<String, Int> =
        if (raw.isNullOrEmpty()) emptyMap() else try {
            (Json.parseToJsonElement(raw) as JsonObject)
                .mapNotNull { (k, v) -> (v as? JsonPrimitive)?.intOrNull?.let { k to it } }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }

    private fun encodeStringMap(map: Map<String, String>): String =
        JsonObject(map.mapValues { JsonPrimitive(it.value) }).toString()

    private fun decodeStringMap(raw: String?): Map<String, String> =
        if (raw.isNullOrEmpty()) emptyMap() else try {
            (Json.parseToJsonElement(raw) as JsonObject)
                .mapNotNull { (k, v) -> (v as? JsonPrimitive)?.contentOrNull?.let { k to it } }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
}
