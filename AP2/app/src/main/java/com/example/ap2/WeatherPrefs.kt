package com.example.ap2

import android.content.Context
import android.content.SharedPreferences

object WeatherPrefs {
    private const val PREFS = "weather_prefs"
    private const val KEY_TEMP = "temp"
    private const val KEY_HUMIDITY = "humidity"
    private const val KEY_CONDITION = "condition"
    private const val KEY_UPDATED_AT = "updated_at"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, temp: String, humidity: String, condition: String, updatedAt: Long = System.currentTimeMillis()) {
        prefs(context).edit()
            .putString(KEY_TEMP, temp)
            .putString(KEY_HUMIDITY, humidity)
            .putString(KEY_CONDITION, condition)
            .putLong(KEY_UPDATED_AT, updatedAt)
            .apply()
    }

    fun getTemp(context: Context): String = prefs(context).getString(KEY_TEMP, "--℃") ?: "--℃"
    fun getHumidity(context: Context): String = prefs(context).getString(KEY_HUMIDITY, "Humidity: --%") ?: "Humidity: --%"
    fun getCondition(context: Context): String = prefs(context).getString(KEY_CONDITION, "--") ?: "--"
    fun getUpdatedAt(context: Context): Long = prefs(context).getLong(KEY_UPDATED_AT, 0L)

    fun saveLocation(context: Context, lat: Double, lon: Double) {
        prefs(context).edit()
            .putString(KEY_LAT, lat.toString())
            .putString(KEY_LON, lon.toString())
            .apply()
    }

    fun getLatitude(context: Context): Double =
        prefs(context).getString(KEY_LAT, null)?.toDoubleOrNull() ?: 19.0760

    fun getLongitude(context: Context): Double =
        prefs(context).getString(KEY_LON, null)?.toDoubleOrNull() ?: 72.8777
}
