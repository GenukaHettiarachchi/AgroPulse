package com.example.ap2

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.databinding.ActivityRemindersBinding
import android.widget.ArrayAdapter
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var adapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupBottomNav()

        binding.btnAddReminder.setOnClickListener {
            startActivity(android.content.Intent(this, NewReminderActivity::class.java))
        }
    }

    private fun setupRecycler() {
        binding.recyclerReminders.layoutManager = LinearLayoutManager(this)
        adapter = ReminderAdapter(emptyList(), onEdit = { item ->
            showEditDialog(item)
        }, onDelete = { item ->
            ReminderRepository.deleteById(this, item.id)
            refreshReminders()
        })
        binding.recyclerReminders.adapter = adapter
        refreshReminders()
    }

    override fun onResume() {
        super.onResume()
        refreshReminders()
    }

    private fun refreshReminders() {
        val list = ReminderRepository.getAll(this).map {
            ReminderItem(
                id = it.savedAt,
                title = it.title,
                schedule = it.timeLabel,
                repeat = it.frequency
            )
        }
        adapter.setItems(list)
    }

    private fun showEditDialog(item: ReminderItem) {
        val ctx = this
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
            .setTitle("Edit Reminder")
        val view = layoutInflater.inflate(R.layout.dialog_edit_reminder, null)
        val etTitle = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTitle)
        val etTime = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTime)
        val etFreq = view.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.dropdownFrequency)
        val inputTime = view.findViewById<TextInputLayout>(R.id.inputTime)

        // Set up frequency dropdown options to match NewReminderActivity
        val freqItems = listOf("Daily", "Weekly", "Every 3 days", "Every 2 weeks")
        val freqAdapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, freqItems)
        etFreq.setAdapter(freqAdapter)

        etTitle.setText(item.title)
        etTime.setText(item.schedule)
        // If current value isn't in the list, default to first
        if (freqItems.contains(item.repeat)) {
            etFreq.setText(item.repeat, false)
        } else {
            etFreq.setText(freqItems.first(), false)
        }

        // Time picker behavior (click field or end icon)
        val calendar = Calendar.getInstance()
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        // Try to keep existing time; if parsing fails, keep current calendar time
        // Simple parsing attempt based on formatter
        // Note: lenient parsing; failures are ignored
        try {
            val parsed = timeFormatter.parse(item.schedule)
            if (parsed != null) {
                calendar.time = parsed
            }
        } catch (_: Exception) { }

        fun openTimePicker() {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(ctx, { _, h, m ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)
                etTime.setText(timeFormatter.format(calendar.time))
            }, hour, minute, false).show()
        }

        inputTime.setEndIconOnClickListener { openTimePicker() }
        etTime.setOnClickListener { openTimePicker() }

        dialog.setView(view)
        dialog.setPositiveButton("Save") { d, _ ->
            val updated = ReminderRepository.Reminder(
                title = etTitle.text?.toString() ?: item.title,
                timeLabel = etTime.text?.toString() ?: item.schedule,
                frequency = etFreq.text?.toString() ?: item.repeat,
                notes = "",
                savedAt = item.id
            )
            ReminderRepository.updateById(ctx, item.id, updated)
            refreshReminders()
            d.dismiss()
        }
        dialog.setNegativeButton("Cancel") { d, _ -> d.dismiss() }
        dialog.show()
    }

    private fun setupBottomNav() {
        val bottom = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_reminders
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { startActivity(android.content.Intent(this, MainActivity::class.java)); true }
                R.id.nav_calendar -> { startActivity(android.content.Intent(this, CalendarActivity::class.java)); true }
                R.id.nav_logs -> { startActivity(android.content.Intent(this, AllLogsActivity::class.java)); true }
                R.id.nav_reminders -> true
                R.id.nav_settings -> { startActivity(android.content.Intent(this, SettingsActivity::class.java)); true }
                else -> true
            }
        }
    }
}


