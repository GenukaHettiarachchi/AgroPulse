package com.example.ap2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Very small repository that persists activities in SharedPreferences as JSON.
 * Keyed by date (yyyy-MM-dd) -> JSON array of entries
 */
object ActivityRepository {
    private const val PREFS = "activities_prefs"
    private const val KEY_PREFIX = "day_" // followed by yyyy-MM-dd
    private val dateKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFmt = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    data class Entry(
        val date: Date,
        val category: String,
        val notes: String,
        val waterLiters: Double?,
        val timeLabel: String, // e.g., 08:00 AM (used in calendar list)
        val savedAt: Long = System.currentTimeMillis()
    )

    fun save(context: Context, entry: Entry) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_PREFIX + dateKeyFmt.format(entry.date)
        val arr = JSONArray(prefs.getString(key, "[]"))
        arr.put(JSONObject().apply {
            put("date", dateKeyFmt.format(entry.date))
            put("category", entry.category)
            put("notes", entry.notes)
            put("water", entry.waterLiters ?: JSONObject.NULL)
            put("time", entry.timeLabel)
            put("savedAt", entry.savedAt)
        })
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun getForDate(context: Context, date: Date): List<Entry> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_PREFIX + dateKeyFmt.format(date)
        val str = prefs.getString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(str)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val d = dateKeyFmt.parse(o.optString("date")) ?: date
                val category = o.optString("category")
                val notes = o.optString("notes")
                val water = if (o.isNull("water")) null else o.optDouble("water")
                val time = o.optString("time")
                val savedAt = if (o.has("savedAt")) o.optLong("savedAt") else System.currentTimeMillis()
                Entry(d, category, notes, water, time, savedAt)
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun getAll(context: Context): List<Entry> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val all = prefs.all
        val result = mutableListOf<Entry>()
        for ((k, v) in all) {
            if (!k.startsWith(KEY_PREFIX)) continue
            val str = v as? String ?: continue
            try {
                val arr = JSONArray(str)
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val d = dateKeyFmt.parse(o.optString("date")) ?: continue
                    val category = o.optString("category")
                    val notes = o.optString("notes")
                    val water = if (o.isNull("water")) null else o.optDouble("water")
                    val time = o.optString("time")
                    val savedAt = if (o.has("savedAt")) o.optLong("savedAt") else System.currentTimeMillis()
                    result.add(Entry(d, category, notes, water, time, savedAt))
                }
            } catch (_: Throwable) {
                // ignore malformed
            }
        }
        return result.sortedByDescending { it.savedAt }
    }
}
