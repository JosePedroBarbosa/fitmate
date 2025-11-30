package com.example.fitmate.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedNotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.showReminder(applicationContext, "Workout Reminder", "Don't forget today's workout")
        val dao = DatabaseProvider.get(applicationContext).cachedNotificationDao()
        withContext(Dispatchers.IO) {
            dao.insert(
                CachedNotificationEntity(
                    title = "Workout Reminder",
                    description = "Don't forget today's workout",
                    timestamp = System.currentTimeMillis(),
                    read = false,
                    type = "reminder"
                )
            )
        }
        return Result.success()
    }
}
