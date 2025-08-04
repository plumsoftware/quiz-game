package ru.plumsoftware.game.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DailyNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Show the daily notification
            val notificationManager = NotificationManager(context)
            notificationManager.showDailyNotification()
            
            // Add a small delay to ensure notification is processed
            delay(1000)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 