package ru.plumsoftware.game.data

/** Подсказки (ТЗ §5.5, §5.7). id совпадают с [HintIds], iconKey — с icons/emoji/hint_*. */
enum class PowerUpType(
    val id: String,
    val title: String,
    val shortTitle: String,
    val description: String,
    val price: Int,
    val emoji: String,
    val iconKey: String
) {
    FIFTY_FIFTY(HintIds.FIFTY, "50 на 50", "50/50", "Убирает 2 неверных ответа", 150, "✂️", "hint_fifty"),
    FREEZE_TIME(HintIds.FREEZE, "Стоп-время", "Стоп", "Останавливает таймер до конца вопроса", 120, "⏸️", "hint_freeze"),
    EXTRA_LIFE(HintIds.LIFE, "Доп. жизнь", "Жизнь", "Продолжить, когда жизни закончились", 200, "❤️", "hint_life"),
    SKIP_QUESTION(HintIds.SKIP, "Пропуск", "Пропуск", "Следующий вопрос без потери жизни", 100, "⏭️", "hint_skip");

    companion object {
        fun fromId(id: String): PowerUpType? = entries.find { it.id == id }
    }
}
