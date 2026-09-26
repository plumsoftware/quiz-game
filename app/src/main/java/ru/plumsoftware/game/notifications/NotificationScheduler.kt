package ru.plumsoftware.game.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Локальные напоминания (ТЗ §11): раз в сутки в 18:00 воркер решает, что показать
 * (и показывать ли вообще — см. [DailyNotificationWorker]).
 */
class NotificationScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME = "daily_reminder_18"
        private val REMIND_AT: LocalTime = LocalTime.of(18, 0)
    }

    /** Включает или выключает напоминания (настройка «Напоминания»). */
    fun setEnabled(enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(REMIND_AT), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        // KEEP — не сбрасываем расписание при каждом запуске приложения.
        wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun delayUntil(time: LocalTime): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(time)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }
}
