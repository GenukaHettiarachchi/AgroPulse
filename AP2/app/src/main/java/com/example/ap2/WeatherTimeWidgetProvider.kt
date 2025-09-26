package com.example.ap2

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherTimeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Ensure we fetch fresh weather data when the widget updates
        // so first-time users or after reboot see data without opening the app.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }

    companion object {
        private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WeatherTimeWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(thisWidget)
            for (id in ids) updateAppWidget(context, manager, id)
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather_time)

            val now = Date()
            views.setTextViewText(R.id.tvWidgetTime, timeFormatter.format(now))
            views.setTextViewText(R.id.tvWidgetTemp, WeatherPrefs.getTemp(context))
            views.setTextViewText(R.id.tvWidgetCondition, WeatherPrefs.getCondition(context))
            views.setTextViewText(R.id.tvWidgetHumidity, WeatherPrefs.getHumidity(context))

            // Tap widget to open app
            val intent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetTime, pi)
            views.setOnClickPendingIntent(R.id.tvWidgetTemp, pi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
