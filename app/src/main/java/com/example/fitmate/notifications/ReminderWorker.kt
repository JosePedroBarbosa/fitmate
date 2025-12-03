package com.example.fitmate.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedNotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.fitmate.data.FirebaseRepository
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitmate.R

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val title = applicationContext.getString(R.string.workout_reminder_title)
        val body = applicationContext.getString(R.string.workout_reminder_body)
        NotificationHelper.showReminder(applicationContext, title, body)
        val dao = DatabaseProvider.get(applicationContext).cachedNotificationDao()
        withContext(Dispatchers.IO) {
            dao.insert(
                CachedNotificationEntity(
                    title = title,
                    description = body,
                    timestamp = System.currentTimeMillis(),
                    read = false,
                    type = "reminder"
                )
            )
        }
        FirebaseRepository.ensureTodayCommunityChallenge { _ -> }
        return Result.success()
    }
}
