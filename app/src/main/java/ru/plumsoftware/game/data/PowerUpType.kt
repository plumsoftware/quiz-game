package ru.plumsoftware.game.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PowerUpType(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val emoji: String,
    val icon: ImageVector,
    val color: Color
) {
    HINT(
        id = "hint",
        title = "Подсказка",
        description = "Показывает категорию и уровень сложности вопроса",
        price = 30,
        emoji = "💡",
        icon = Icons.Default.Lightbulb,
        color = Color(0xFFFF9800)
    ),
    FIFTY_FIFTY(
        id = "fifty_fifty",
        title = "50/50",
        description = "Убирает два неверных варианта ответа",
        price = 50,
        emoji = "✂️",
        icon = Icons.Default.ContentCut,
        color = Color(0xFF2196F3)
    ),
    EXTRA_LIFE(
        id = "extra_life",
        title = "Доп. жизнь",
        description = "Восстанавливает одно сердце",
        price = 80,
        emoji = "❤️",
        icon = Icons.Default.Favorite,
        color = Color(0xFFE91E63)
    ),
    EXTRA_TIME(
        id = "extra_time",
        title = "+10 секунд",
        description = "Добавляет 10 секунд к таймеру вопроса",
        price = 40,
        emoji = "⏱️",
        icon = Icons.Default.Timer,
        color = Color(0xFF00BCD4)
    ),
    SKIP_QUESTION(
        id = "skip_question",
        title = "Пропуск",
        description = "Пропустить вопрос без потери жизни",
        price = 60,
        emoji = "⏭️",
        icon = Icons.Default.SkipNext,
        color = Color(0xFF9C27B0)
    ),
    REVEAL_ANSWER(
        id = "reveal_answer",
        title = "Ответ",
        description = "Подсвечивает правильный вариант",
        price = 100,
        emoji = "✅",
        icon = Icons.Default.CheckCircle,
        color = Color(0xFF4CAF50)
    ),
    SHIELD(
        id = "shield",
        title = "Щит",
        description = "Защищает от следующей ошибки",
        price = 70,
        emoji = "🛡️",
        icon = Icons.Default.Shield,
        color = Color(0xFF607D8B)
    ),
    FREEZE_TIME(
        id = "freeze_time",
        title = "Заморозка",
        description = "Останавливает таймер на 8 секунд",
        price = 45,
        emoji = "❄️",
        icon = Icons.Default.AcUnit,
        color = Color(0xFF03A9F4)
    ),
    LUCKY_HINT(
        id = "lucky_hint",
        title = "Удачный намёк",
        description = "Подсвечивает один правильный вариант",
        price = 75,
        emoji = "🍀",
        icon = Icons.Default.Star,
        color = Color(0xFF8BC34A)
    ),
    SECOND_CHANCE(
        id = "second_chance",
        title = "Второй шанс",
        description = "Позволяет ответить ещё раз после ошибки",
        price = 90,
        emoji = "🔄",
        icon = Icons.Default.Replay,
        color = Color(0xFFFF5722)
    ),
    DOUBLE_COINS(
        id = "double_coins",
        title = "×2 монеты",
        description = "Удваивает монеты за следующий правильный ответ",
        price = 55,
        emoji = "🪙",
        icon = Icons.Default.MonetizationOn,
        color = Color(0xFFFFD700)
    ),
    SLOW_TIMER(
        id = "slow_timer",
        title = "Медленный таймер",
        description = "Таймер идёт в 2 раза медленнее на вопросе",
        price = 35,
        emoji = "🐢",
        icon = Icons.Default.Speed,
        color = Color(0xFF795548)
    );

    companion object {
        fun fromId(id: String): PowerUpType? = entries.find { it.id == id }
    }
}
