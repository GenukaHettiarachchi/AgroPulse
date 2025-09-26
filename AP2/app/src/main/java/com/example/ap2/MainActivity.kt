package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.Geocoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recentAdapter: RecentActivityAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = (perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                || perms[Manifest.permission.ACCESS_FINE_LOCATION] == true)
        if (granted) {
            fetchAndSaveLocationAndWeather()
        } else {
            // Proceed with default/fallback coordinates
            fetchWeather()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure notification channel exists for reminders (Android O+)
        NotificationUtil.createChannels(this)


        setupQuickActions()
        setupRecycler()
        setupBottomNav()
        loadWeatherFromPrefs()
        updateLocationDisplay()
        startClock()
        schedulePeriodicWeatherUpdates()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ensureLocationPermissionThenUpdate()

        binding.btnRefreshLocation.setOnClickListener {
            ensureLocationPermissionThenUpdate()
        }
    }

    private fun setupQuickActions() {
        setQuickAction(binding.qaAdd, R.drawable.add, "Add Activity")
        setQuickAction(binding.qaCalendar, R.drawable.calender, "Calendar View")
        setQuickAction(binding.qaLogs, R.drawable.log, "All Logs")
        setQuickAction(binding.qaExport, R.drawable.export, "Export Data")

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
        val today = Date()
        val entries = ActivityRepository.getForDate(this, today).sortedByDescending { it.savedAt }
        val items = entries.map {
            val icon = R.drawable.log
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
        // Use saved location if available; otherwise defaults come from WeatherPrefs
        val latitude = WeatherPrefs.getLatitude(this)
        val longitude = WeatherPrefs.getLongitude(this)
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

    private fun ensureLocationPermissionThenUpdate() {
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (coarseGranted || fineGranted) {
            fetchAndSaveLocationAndWeather()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun fetchAndSaveLocationAndWeather() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        WeatherPrefs.saveLocation(this, loc.latitude, loc.longitude)
                        updateLocationDisplay()
                    }
                    // Fetch weather using (possibly new) saved location
                    fetchWeather()
                }
                .addOnFailureListener {
                    // Fall back to default
                    updateLocationDisplay()
                    fetchWeather()
                }
        } catch (_: SecurityException) {
            // Permission revoked mid-operation; fall back
            updateLocationDisplay()
            fetchWeather()
        }
    }

    private fun updateLocationDisplay() {
        val lat = WeatherPrefs.getLatitude(this)
        val lon = WeatherPrefs.getLongitude(this)
        var label: String? = null
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val results = geocoder.getFromLocation(lat, lon, 1)
            if (!results.isNullOrEmpty()) {
                val addr = results[0]
                label = listOfNotNull(addr.locality, addr.adminArea).joinToString(", ")
            }
        } catch (_: Exception) {
            // Geocoder may fail on some devices; fall back to coords
        }
        if (label.isNullOrBlank()) {
            label = String.format(Locale.getDefault(), "%.4f, %.4f", lat, lon)
        }
        binding.tvLocation.text = "Location: $label"
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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }
}


