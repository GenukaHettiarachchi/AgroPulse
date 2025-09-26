package com.example.ap2

import android.content.Context
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
            val latitude = 19.0760
            val longitude = 72.8777
            val service = WeatherService.create()
            val response = service.getCurrent(latitude, longitude)
            val temp = "${response.current.temperatureC.toInt()}℃"
            val humidity = "Humidity: ${response.current.humidityPercent}%"
            val label = mapWeatherCodeToLabel(response.current.weatherCode)

            WeatherPrefs.save(applicationContext, temp, humidity, label)
            WeatherTimeWidgetProvider.updateAll(applicationContext)
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
