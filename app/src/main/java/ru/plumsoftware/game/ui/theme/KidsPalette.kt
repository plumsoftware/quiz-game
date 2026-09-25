package ru.plumsoftware.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Палитра дизайн-системы «Викторины для детей» (ТЗ §3.1).
 * Только светлая тема.
 */
object Kids {
    // Базовые роли
    val Background = Color(0xFFFFF7EC)
    val BackgroundTop = Color(0xFFEFEAFF) // верх градиента на приветствии
    val TextPrimary = Color(0xFF2B2350)
    val TextSecondary = Color(0xFF7A7396)
    val TextMuted = Color(0xFF9A93B0)

    // Основной (фиолетовый)
    val Primary = Color(0xFF6C4DF6)
    val PrimaryShadow = Color(0xFF4E33C9)
    val PrimarySoft = Color(0xFFEFEAFF)   // светлая подложка (активная вкладка, «НАЧАТЬ!»)
    val PrimarySoftShadow = Color(0xFFE2DAFF)

    // Успех (зелёный)
    val Success = Color(0xFF2DC78A)
    val SuccessShadow = Color(0xFF1FA06C)
    val SuccessSoft = Color(0xFFE6F8EF)

    // Ошибка (красный)
    val Error = Color(0xFFFF5C6C)
    val ErrorShadow = Color(0xFFD93A4B)

    // Монеты (жёлтый)
    val Coin = Color(0xFFFFC93C)
    val CoinShadow = Color(0xFFE0A800)
    val CoinText = Color(0xFFB07A00)
    val CoinSoft = Color(0xFFFFF1C2)

    // Кристаллы (голубой)
    val Gem = Color(0xFF3AA8FF)
    val GemShadow = Color(0xFF1F85D6)

    // Серия (оранжевый градиент)
    val StreakFrom = Color(0xFFFF8A4C)
    val StreakTo = Color(0xFFFF5C8A)
    val StreakShadow = Color(0xFFD9456F)
    val StreakChip = Color(0xFFFFE7D6)
    val StreakChipText = Color(0xFFF2622E)

    // Карточка
    val Card = Color(0xFFFFFFFF)
    val CardShadow = Color(0xFFEDE4D6)

    // Заблокировано
    val Locked = Color(0xFFE8E2F0)
    val LockedShadow = Color(0xFFCFC7DD)

    // Вспомогательные
    val TrackBackground = Color(0xFFEEF0F5)
    val InputBorder = Color(0xFFE2DAFF)
    val SegmentTrack = Color(0xFFF1EADF)
    val Avatar = Color(0xFFFFD9A8)
    val AvatarShadow = Color(0xFFF0C58C)
}

/** Цветовой набор темы викторины (ТЗ §3.1, вторая таблица). */
data class TopicColors(
    val primary: Color,
    val shadow: Color,
    val cardBg: Color
)

/** Ключи тем совпадают с topicId (см. icons/emoji/topic_*). */
val TopicPalette: Map<String, TopicColors> = mapOf(
    "animals" to TopicColors(Color(0xFFFFA629), Color(0xFFD9820A), Color(0xFFFFF0D9)),
    "space" to TopicColors(Color(0xFF7C6CFF), Color(0xFF5846E0), Color(0xFFECE9FF)),
    "countries" to TopicColors(Color(0xFF2DC78A), Color(0xFF1FA06C), Color(0xFFE3F8EE)),
    "math" to TopicColors(Color(0xFFFF6B8B), Color(0xFFDE4A6B), Color(0xFFFFE6EC)),
    "nature" to TopicColors(Color(0xFF48B85C), Color(0xFF339444), Color(0xFFE6F6E4)),
    "science" to TopicColors(Color(0xFF3AA8FF), Color(0xFF1F85D6), Color(0xFFE1F2FF)),
    "fairytales" to TopicColors(Color(0xFFE46BE0), Color(0xFFBC47B8), Color(0xFFFBE6FA)),
    "music" to TopicColors(Color(0xFFFF8A4C), Color(0xFFDC6427), Color(0xFFFFEBDF)),
    "sport" to TopicColors(Color(0xFF26B7B0), Color(0xFF16918B), Color(0xFFDFF6F4)),
    "food" to TopicColors(Color(0xFFF25555), Color(0xFFCC3636), Color(0xFFFFE4E1)),
)

fun topicColors(topicId: String): TopicColors =
    TopicPalette[topicId] ?: TopicColors(Kids.Primary, Kids.PrimaryShadow, Kids.PrimarySoft)
