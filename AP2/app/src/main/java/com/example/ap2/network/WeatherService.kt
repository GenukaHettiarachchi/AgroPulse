package com.example.ap2.network

import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    // Open-Meteo API: https://open-meteo.com/
    @GET("v1/forecast")
    suspend fun getCurrent(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    companion object {
        fun create(): WeatherService = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    @Json(name = "temperature_2m") val temperatureC: Double,
    @Json(name = "relative_humidity_2m") val humidityPercent: Int,
    @Json(name = "weather_code") val weatherCode: Int
)

fun mapWeatherCodeToLabel(code: Int): String = when (code) {
    0 -> "Clear"
    1, 2 -> "Partly Cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    95 -> "Thunderstorm"
    else -> "Cloudy"
}


