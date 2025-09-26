package com.example.ap2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Very small repository to persist reminders in SharedPreferences as JSON.
 */
object ReminderRepository {
    private const val PREFS = "reminders_prefs"
    private const val KEY = "reminders_list"

    data class Reminder(
        val title: String,
        val timeLabel: String,
        val frequency: String,
        val notes: String,
        val savedAt: Long = System.currentTimeMillis()
    )

    fun save(context: Context, reminder: Reminder) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        arr.put(JSONObject().apply {
            put("title", reminder.title)
            put("timeLabel", reminder.timeLabel)
            put("frequency", reminder.frequency)
            put("notes", reminder.notes)
            put("savedAt", reminder.savedAt)
        })
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun getAll(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(str)
            val list = mutableListOf<Reminder>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val title = o.optString("title")
                val timeLabel = o.optString("timeLabel")
                val frequency = o.optString("frequency")
                val notes = o.optString("notes")
                val savedAt = o.optLong("savedAt", System.currentTimeMillis())
                list.add(Reminder(title, timeLabel, frequency, notes, savedAt))
            }
            list.sortedByDescending { it.savedAt }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun deleteById(context: Context, id: Long): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        val newArr = JSONArray()
        var removed = false
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val savedAt = o.optLong("savedAt", -1L)
            if (savedAt == id) {
                removed = true
                continue
            }
            newArr.put(o)
        }
        if (removed) prefs.edit().putString(KEY, newArr.toString()).apply()
        return removed
    }

    fun updateById(context: Context, id: Long, updated: Reminder): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        var changed = false
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val savedAt = o.optLong("savedAt", -1L)
            if (savedAt == id) {
                // keep original id
                val repl = JSONObject().apply {
                    put("title", updated.title)
                    put("timeLabel", updated.timeLabel)
                    put("frequency", updated.frequency)
                    put("notes", updated.notes)
                    put("savedAt", id)
                }
                newArr.put(repl)
                changed = true
            } else {
                newArr.put(o)
            }
        }
        if (changed) prefs.edit().putString(KEY, newArr.toString()).apply()
        return changed
    }
}
