package com.example.ap2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_TITLE = "reminder_title"
        const val KEY_TIME_LABEL = "reminder_time_label"
    }

    override suspend fun doWork(): Result {
        return try {
            val title = inputData.getString(KEY_TITLE) ?: "Reminder"
            val timeLabel = inputData.getString(KEY_TIME_LABEL) ?: ""

            if (Build.VERSION.SDK_INT >= 33) {
                val granted = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    Log.w("ReminderWorker", "POST_NOTIFICATIONS not granted; skipping notification")
                    return Result.success()
                }
            }

            NotificationUtil.postReminder(
                applicationContext,
                notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                title = title,
                content = if (timeLabel.isNotBlank()) "Scheduled at $timeLabel" else "Reminder"
            )
            Result.success()
        } catch (t: Throwable) {
            Log.e("ReminderWorker", "Failed to post reminder", t)
            Result.retry()
        }
    }
}
