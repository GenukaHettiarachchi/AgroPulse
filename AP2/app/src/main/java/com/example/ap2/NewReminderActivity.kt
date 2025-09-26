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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


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

        // Schedule a one-time notification for the selected time
        scheduleReminderNotification(title, timeLabel)
    }

    private fun scheduleReminderNotification(title: String, timeLabel: String) {
        val nextTriggerMillis = tryComputeNextTriggerMillis(timeLabel)
        val delay = (nextTriggerMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_TIME_LABEL, timeLabel)
            .build()
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(req)
    }

    private fun tryComputeNextTriggerMillis(timeLabel: String): Long {
        val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        try {
            val parsed = fmt.parse(timeLabel)
            if (parsed != null) {
                target.time = parsed
                // keep today's date but use parsed hour/minute
                val hour = target.get(Calendar.HOUR_OF_DAY)
                val minute = target.get(Calendar.MINUTE)
                target.timeInMillis = now.timeInMillis
                target.set(Calendar.SECOND, 0)
                target.set(Calendar.MILLISECOND, 0)
                target.set(Calendar.HOUR_OF_DAY, hour)
                target.set(Calendar.MINUTE, minute)
                if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
        } catch (_: Exception) { }
        // fallback: 1 minute from now
        return now.timeInMillis + 60_000L
    }
}
