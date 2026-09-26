package ru.plumsoftware.game.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.plumsoftware.game.data.GameManager
import ru.plumsoftware.game.data.StreakLogic
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Ежедневная проверка в 18:00 (ТЗ §11):
 * - напоминание про серию, только если сегодня не играли;
 * - «Сундук ждёт тебя!» — раз в 3 дня неактивности;
 * - не больше 1 уведомления в сутки, выключается в настройках.
 */
class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val manager = GameManager(applicationContext)
            val state = manager.current()
            val today = LocalDate.now()
            if (!state.profileCreated || !state.settings.notifications) return Result.success()
            if (state.lastPlayedDate == today) return Result.success()
            if (manager.lastNotificationDate() == today.toString()) return Result.success()

            val name = state.playerName
            val inactiveDays = state.lastPlayedDate?.let { ChronoUnit.DAYS.between(it, today) } ?: 0L
            val (title, text) = when {
                inactiveDays >= 3 && inactiveDays % 3 == 0L ->
                    "Сундук ждёт тебя! 🎁" to "$name, загляни в игру — на карте тебя ждёт награда."
                state.streakDays > 0 ->
                    "$name, твоя серия 🔥 ${state.streakDays} ${StreakLogic.daysWord(state.streakDays)} ждёт!" to
                        "Пройди один уровень, чтобы не потерять серию."
                else ->
                    "$name, новые вопросы ждут!" to "Сыграй уровень и начни новую серию 🔥"
            }
            NotificationManager(applicationContext).show(title, text)
            manager.setLastNotificationDate(today.toString())
            Result.success()
        } catch (e: Exception) {
            Result.success()
        }
    }
}
