package com.example.ap2

import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.databinding.ActivityCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    private val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val headerMonthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupCalendar()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        // Refresh list based on currently selected date when returning to this screen
        val current = Date(binding.calendarView.date)
        updateMonthHeader(current)
        updateActivitiesForDate(current)
    }

    private fun setupRecycler() {
        binding.recyclerDay.layoutManager = LinearLayoutManager(this)
        binding.recyclerDay.adapter = DayActivitiesAdapter(emptyList())
    }

    private fun setupCalendar() {
        val today = Calendar.getInstance().time
        updateMonthHeader(today)
        updateActivitiesForDate(today)

        binding.calendarView.setOnDateChangeListener { _: CalendarView, year: Int, month: Int, day: Int ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day, 0, 0, 0)
            val selected = cal.time
            updateMonthHeader(selected)
            updateActivitiesForDate(selected)
        }

        binding.btnToday.setOnClickListener {
            val now = Calendar.getInstance()
            binding.calendarView.date = now.timeInMillis
            updateMonthHeader(now.time)
            updateActivitiesForDate(now.time)
        }
    }

    private fun updateMonthHeader(date: Date) {
        binding.tvMonth.text = headerMonthFormatter.format(date)
        // no-op here; the selected date is shown in the activities header
    }

    private fun updateActivitiesForDate(date: Date) {
        val entries = ActivityRepository.getForDate(this, date)
        val list = entries.map {
            DayActivityItem(
                iconResId = R.drawable.ic_placeholder,
                title = it.category,
                subtitle = it.notes,
                time = it.timeLabel
            )
        }
        (binding.recyclerDay.adapter as DayActivitiesAdapter).submit(list)
        binding.tvActivitiesHeader.text = "Activities on ${dateFormatter.format(date)}"
    }

    private fun setupBottomNav() {
        val bottom = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_calendar
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { startActivity(android.content.Intent(this, MainActivity::class.java)); true }
                R.id.nav_calendar -> true
                R.id.nav_logs -> { startActivity(android.content.Intent(this, AllLogsActivity::class.java)); true }
                R.id.nav_reminders -> { startActivity(android.content.Intent(this, RemindersActivity::class.java)); true }
                R.id.nav_settings -> { startActivity(android.content.Intent(this, SettingsActivity::class.java)); true }
                else -> true
            }
        }
    }
}


