package com.example.ap2

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivityNewActivityBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter

class NewActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewActivityBinding
    private val calendar: Calendar = Calendar.getInstance()
    private val dateDisplayFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val timeLabelFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val categories = listOf("Storage Checking", "Watering", "Pesticide")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        setupCategoryDropdown()
        prefillWeather()

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener {
            saveActivity()
            finish()
        }

        // Initialize date field with current date
        binding.etDate.setText(dateDisplayFormatter.format(calendar.time))
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            binding.etDate.setText(dateDisplayFormatter.format(calendar.time))
        }, year, month, day).show()
    }

    private fun prefillWeather() {
        binding.tvWeatherTemp.text = WeatherPrefs.getTemp(this)
        binding.tvWeatherCond.text = WeatherPrefs.getCondition(this)
    }

    private fun saveActivity() {
        val dateText = binding.etDate.text?.toString()
        val date: Date = try {
            if (dateText.isNullOrBlank()) calendar.time else dateDisplayFormatter.parse(dateText) ?: calendar.time
        } catch (_: Throwable) {
            calendar.time
        }
        val category = binding.dropdownCategory.text?.toString()?.ifBlank { "Activity" } ?: "Activity"
        val notes = binding.etNotes.text?.toString() ?: ""
        val water = binding.etWater.text?.toString()?.toDoubleOrNull()
        val timeLabel = timeLabelFormatter.format(Calendar.getInstance().time)

        ActivityRepository.save(this, ActivityRepository.Entry(
            date = date,
            category = category,
            notes = notes,
            waterLiters = water,
            timeLabel = timeLabel
        ))
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        binding.dropdownCategory.setAdapter(adapter)
        // Default selection
        binding.dropdownCategory.setText(categories.getOrNull(1) ?: categories.first(), false)
        // Initialize preview with default
        updatePreviewForCategory(binding.dropdownCategory.text?.toString())

        binding.dropdownCategory.setOnItemClickListener { _, _, position, _ ->
            val selected = categories.getOrNull(position)
            updatePreviewForCategory(selected)
        }
    }

    private fun updatePreviewForCategory(category: String?) {
        val label = category ?: "Current Weather"
        binding.tvWeatherTitle.text = label
        val icon = when (category) {
            "Storage Checking" -> R.drawable.ic_placeholder
            "Watering" -> R.drawable.ic_placeholder
            "Pesticide" -> R.drawable.ic_placeholder
            else -> R.drawable.ic_weather
        }
        binding.ivWeatherIcon.setImageResource(icon)
    }
}
