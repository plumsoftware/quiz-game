package ru.plumsoftware.game.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Состояние серии дней (ТЗ §2.1 streak, §6.6). */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val lastPlayed: LocalDate? = null,
    val freezes: Int = 0,
    val frozenDates: Set<LocalDate> = emptySet(),
    val playedDates: Set<LocalDate> = emptySet()
)

/** Награды за серию (§5.10). */
enum class StreakMilestone(val days: Int, val title: String, val emoji: String) {
    D3(3, "100 🪙", "🪙"),
    D7(7, "Сундук с подсказками", "🎁"),
    D14(14, "Персонаж 🦄 Единорог", "🦄"),
    D30(30, "Золотая рамка профиля + 20 💎", "👑");

    val key: String get() = days.toString()
}

enum class WeekDayState { PLAYED, FROZEN, TODAY, TODAY_PLAYED, MISSED, FUTURE }

object StreakLogic {
    /** Сколько дней хранить историю сыгранных дат. */
    private const val HISTORY_DAYS = 40L

    /**
     * Вызывается при запуске и перед начислением дня.
     * Если были пропущены дни — тратит заморозки (по одной на день) или сбрасывает серию до 0.
     * Защита от перевода часов назад: если сегодня раньше lastPlayed — ничего не делаем.
     */
    fun onAppOpen(s: StreakState, today: LocalDate): StreakState {
        val last = s.lastPlayed ?: return s
        if (!today.isAfter(last)) return s
        val gap = ChronoUnit.DAYS.between(last, today)
        if (gap <= 1) return s
        if (s.current <= 0) return s
        val missed = (gap - 1).toInt()
        return if (s.freezes >= missed) {
            val frozen = (1..missed).map { last.plusDays(it.toLong()) }
            s.copy(
                freezes = s.freezes - missed,
                lastPlayed = today.minusDays(1),
                frozenDates = trim(s.frozenDates + frozen, today)
            )
        } else {
            s.copy(current = 0)
        }
    }

    /**
     * Засчитывает день, когда пройден уровень.
     * @return новое состояние и признак того, что день засчитан впервые.
     */
    fun onLevelPassed(state: StreakState, today: LocalDate): Pair<StreakState, Boolean> {
        val s = onAppOpen(state, today)
        val last = s.lastPlayed
        if (last != null && today.isBefore(last)) return s to false // часы переведены назад
        if (last == today) return s to false
        val newCurrent = if (last != null && ChronoUnit.DAYS.between(last, today) == 1L && s.current > 0) {
            s.current + 1
        } else 1
        return s.copy(
            current = newCurrent,
            best = maxOf(s.best, newCurrent),
            lastPlayed = today,
            playedDates = trim(s.playedDates + today, today)
        ) to true
    }

    /** Вехи, которые достигнуты текущей серией и ещё не выданы. */
    fun newMilestones(current: Int, claimed: Set<String>): List<StreakMilestone> =
        StreakMilestone.entries.filter { current >= it.days && it.key !in claimed }

    /** Следующая невыданная веха (для подписи «Ещё K дней до …»). */
    fun nextMilestone(current: Int, claimed: Set<String>): StreakMilestone? =
        StreakMilestone.entries.firstOrNull { it.days > current && it.key !in claimed }

    /** Состояние дней текущей недели Пн–Вс. */
    fun week(today: LocalDate, played: Set<LocalDate>, frozen: Set<LocalDate>): List<WeekDayState> {
        val monday = today.with(DayOfWeek.MONDAY)
        return (0L until 7L).map { i ->
            val d = monday.plusDays(i)
            when {
                d == today -> if (d in played) WeekDayState.TODAY_PLAYED else WeekDayState.TODAY
                d.isAfter(today) -> WeekDayState.FUTURE
                d in played -> WeekDayState.PLAYED
                d in frozen -> WeekDayState.FROZEN
                else -> WeekDayState.MISSED
            }
        }
    }

    /** Склонение «день/дня/дней». */
    fun daysWord(n: Int): String {
        val mod100 = n % 100
        val mod10 = n % 10
        return when {
            mod100 in 11..14 -> "дней"
            mod10 == 1 -> "день"
            mod10 in 2..4 -> "дня"
            else -> "дней"
        }
    }

    private fun trim(dates: Set<LocalDate>, today: LocalDate): Set<LocalDate> =
        dates.filter { !it.isBefore(today.minusDays(HISTORY_DAYS)) }.toSet()
}
