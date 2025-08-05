package ru.plumsoftware.game.notifications

import android.content.Context
import androidx.work.*
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val DAILY_NOTIFICATION_WORK = "daily_notification_work"
        private const val QUIZ_REMINDER_WORK = "quiz_reminder_work"
    }

    fun scheduleDailyNotification() {
        // Cancel any existing daily notification work
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_NOTIFICATION_WORK)

        // Create a periodic work request for daily notifications
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        // Enqueue the periodic work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_NOTIFICATION_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }

    fun scheduleQuizReminder() {
        // Cancel any existing quiz reminder work
        WorkManager.getInstance(context).cancelUniqueWork(QUIZ_REMINDER_WORK)

        // Create a periodic work request for quiz reminders (every 12 hours)
        val quizWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            12, TimeUnit.HOURS
        ).setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        // Enqueue the periodic work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            QUIZ_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            quizWorkRequest
        )
    }

    fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_NOTIFICATION_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(QUIZ_REMINDER_WORK)
        WorkManager.getInstance(context).cancelAllWork()
    }

    private fun calculateInitialDelay(): Long {
        // Schedule notification for 9:00 AM
        val targetTime = LocalTime.of(9, 0)
        val now = LocalTime.now()

        var delay = targetTime.toSecondOfDay() - now.toSecondOfDay()
        if (delay <= 0) {
            delay += 24 * 60 * 60 // Add 24 hours if target time has passed
        }

        return delay * 1000L // Convert to milliseconds
    }
} 