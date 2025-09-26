package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.databinding.ActivityMainBinding
import com.example.ap2.databinding.ItemQuickActionBinding
import com.example.ap2.network.WeatherService
import com.example.ap2.network.mapWeatherCodeToLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recentAdapter: RecentActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupQuickActions()
        setupRecycler()
        setupBottomNav()
        loadWeatherFromPrefs()
        startClock()
        schedulePeriodicWeatherUpdates()
        fetchWeather()
    }

    private fun setupQuickActions() {
        setQuickAction(binding.qaAdd, R.drawable.ic_placeholder, "Add Activity")
        setQuickAction(binding.qaCalendar, R.drawable.ic_calendar, "Calendar View")
        setQuickAction(binding.qaLogs, R.drawable.ic_list, "All Logs")
        setQuickAction(binding.qaExport, R.drawable.ic_placeholder, "Export Data")

        binding.qaAdd.root.setOnClickListener {
            startActivity(Intent(this, NewActivityActivity::class.java))
        }
        binding.qaCalendar.root.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
        binding.qaLogs.root.setOnClickListener {
            startActivity(Intent(this, AllLogsActivity::class.java))
        }
    }

    private fun setQuickAction(binding: ItemQuickActionBinding, iconRes: Int, label: String) {
        binding.ivIcon.setImageResource(iconRes)
        binding.tvLabel.text = label
    }

    private fun setupRecycler() {
        binding.recyclerRecent.layoutManager = LinearLayoutManager(this)
        recentAdapter = RecentActivityAdapter(emptyList())
        binding.recyclerRecent.adapter = recentAdapter
        refreshRecent()
    }

    override fun onResume() {
        super.onResume()
        refreshRecent()
    }

    private fun refreshRecent() {
        val entries = ActivityRepository.getAll(this).take(3)
        val items = entries.map {
            val icon = R.drawable.ic_placeholder
            RecentActivityItem(
                iconResId = icon,
                title = it.category,
                subtitle = it.notes,
                timeAgo = computeTimeAgo(it.savedAt)
            )
        }
        recentAdapter.submit(items)
    }

    private fun computeTimeAgo(savedAt: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - savedAt
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour
        return when {
            diff < minute -> "Just now"
            diff < hour -> "${diff / minute}m ago"
            diff < day -> "${diff / hour}h ago"
            else -> "${diff / day}d ago"
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_logs -> {
                    startActivity(Intent(this, AllLogsActivity::class.java))
                    true
                }
                R.id.nav_reminders -> {
                    startActivity(Intent(this, RemindersActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> true
            }
        }
    }

    private fun fetchWeather() {
        // Default location: Mumbai as example. Replace with user's location if available.
        val latitude = 19.0760
        val longitude = 72.8777
        val service = WeatherService.create()
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    service.getCurrent(latitude, longitude)
                }
                val temp = "${response.current.temperatureC.toInt()}℃"
                val humidity = "Humidity: ${response.current.humidityPercent}%"
                val label = mapWeatherCodeToLabel(response.current.weatherCode)
                binding.tvTemp.text = temp
                binding.tvHumidity.text = humidity
                binding.tvCondition.text = label
                // Persist and update widget
                WeatherPrefs.save(this@MainActivity, temp, humidity, label)
                WeatherTimeWidgetProvider.updateAll(this@MainActivity)
            } catch (_: Exception) {
                // Keep defaults on failure
            }
        }
    }

    private fun loadWeatherFromPrefs() {
        binding.tvTemp.text = WeatherPrefs.getTemp(this)
        binding.tvHumidity.text = WeatherPrefs.getHumidity(this)
        binding.tvCondition.text = WeatherPrefs.getCondition(this)
    }

    private fun startClock() {
        val timeFormatter = SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault())
        lifecycleScope.launch(Dispatchers.Main) {
            while (!isFinishing) {
                binding.tvDate?.let { it.text = timeFormatter.format(Date()) }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun schedulePeriodicWeatherUpdates() {
        val work = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(1, TimeUnit.HOURS)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }
}


