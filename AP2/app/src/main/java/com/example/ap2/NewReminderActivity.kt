package com.example.ap2

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivityNewReminderBinding
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class NewReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReminderBinding
    private val calendar: Calendar = Calendar.getInstance()
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTimePicker()
        setupDropdown()

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener {
            saveReminder()
            finish()
        }

        // Initialize time field with current time
        binding.etTime.setText(timeFormatter.format(calendar.time))
    }

    private fun setupTimePicker() {
        binding.inputTime.setEndIconOnClickListener { showTimePicker() }
        binding.etTime.setOnClickListener { showTimePicker() }
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(this, { _, h, m ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, m)
            binding.etTime.setText(timeFormatter.format(calendar.time))
        }, hour, minute, false).show()
    }

    private fun setupDropdown() {
        val items = listOf("Daily", "Weekly", "Every 3 days", "Every 2 weeks")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        binding.dropdownFrequency.setAdapter(adapter)
        binding.dropdownFrequency.setText(items.first(), false)
        // Improve UX: open dropdown on click and when focused
        binding.dropdownFrequency.setOnClickListener {
            binding.dropdownFrequency.showDropDown()
        }
        binding.dropdownFrequency.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.dropdownFrequency.showDropDown()
        }
    }

    private fun saveReminder() {
        val title = binding.etTitle.text?.toString()?.ifBlank { "Reminder" } ?: "Reminder"
        val timeLabel = binding.etTime.text?.toString()?.ifBlank { timeFormatter.format(calendar.time) }
            ?: timeFormatter.format(calendar.time)
        val frequency = binding.dropdownFrequency.text?.toString() ?: "Daily"
        val notes = binding.etNotes.text?.toString() ?: ""

        ReminderRepository.save(this, ReminderRepository.Reminder(
            title = title,
            timeLabel = timeLabel,
            frequency = frequency,
            notes = notes
        ))
    }
}
