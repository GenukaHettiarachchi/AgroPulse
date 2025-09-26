package com.example.ap2

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ap2.network.WeatherService
import com.example.ap2.network.mapWeatherCodeToLabel

class WeatherUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val latitude = WeatherPrefs.getLatitude(applicationContext)
            val longitude = WeatherPrefs.getLongitude(applicationContext)
            val service = WeatherService.create()
            val response = service.getCurrent(latitude, longitude)
            val temp = "${response.current.temperatureC.toInt()}℃"
            val humidity = "Humidity: ${response.current.humidityPercent}%"
            val label = mapWeatherCodeToLabel(response.current.weatherCode)

            WeatherPrefs.save(applicationContext, temp, humidity, label)
            Log.d("WeatherUpdateWorker", "Weather updated: temp=$temp, humidity=$humidity, condition=$label")
            WeatherTimeWidgetProvider.updateAll(applicationContext)
            Result.success()
        } catch (t: Throwable) {
            Log.e("WeatherUpdateWorker", "Weather update failed", t)
            Result.retry()
        }
    }
}
