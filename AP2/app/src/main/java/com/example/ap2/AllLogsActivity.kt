package com.example.ap2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.databinding.ActivityAllLogsBinding
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.widget.PopupMenu
import java.text.Collator
import androidx.appcompat.app.AlertDialog

class AllLogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllLogsBinding
    private lateinit var adapter: LogItemAdapter
    private val displayDateFmt = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    private val dateKeyFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

    private enum class SortType { ACTIVITY_NAME_AZ, TIME_DESC }
    private var sortType: SortType = SortType.ACTIVITY_NAME_AZ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupSearchAndFilter()
        refreshLogs()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        refreshLogs()
    }

    private fun setupRecycler() {
        binding.recyclerLogs.layoutManager = LinearLayoutManager(this)
        adapter = LogItemAdapter(emptyList()) { item ->
            confirmDelete(item)
        }
        binding.recyclerLogs.adapter = adapter
    }

    private fun refreshLogs() {
        val all = ActivityRepository.getAll(this)

        val sorted = when (sortType) {
            SortType.ACTIVITY_NAME_AZ -> {
                val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
                all.sortedWith(compareBy(collator) { it.category })
            }
            SortType.TIME_DESC -> all.sortedByDescending { it.date }
        }

        val items = sorted.map { entry ->
            LogItem(
                iconResId = R.drawable.log,
                title = entry.category,
                description = entry.notes,
                date = displayDateFmt.format(entry.date),
                savedAt = entry.savedAt,
                srcDateKey = dateKeyFmt.format(entry.date),
                timeLabel = entry.timeLabel
            )
        }
        adapter.setItems(items)
    }

    private fun setupBottomNav() {
        val bottom = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_logs
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { startActivity(android.content.Intent(this, MainActivity::class.java)); true }
                R.id.nav_calendar -> { startActivity(android.content.Intent(this, CalendarActivity::class.java)); true }
                R.id.nav_logs -> true
                R.id.nav_reminders -> { startActivity(android.content.Intent(this, RemindersActivity::class.java)); true }
                R.id.nav_settings -> { startActivity(android.content.Intent(this, SettingsActivity::class.java)); true }
                else -> true
            }
        }
    }

    private fun setupSearchAndFilter() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        binding.btnFilter.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "Sort by Activity Name (A–Z)")
            popup.menu.add(0, 2, 1, "Sort by Time (Newest)")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { sortType = SortType.ACTIVITY_NAME_AZ; refreshLogs(); true }
                    2 -> { sortType = SortType.TIME_DESC; refreshLogs(); true }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun confirmDelete(item: LogItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete log")
            .setMessage("Are you sure you want to delete this log? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val byIdRemoved = ActivityRepository.deleteBySavedAt(this, item.savedAt)
                val removed = if (byIdRemoved) true else ActivityRepository.deleteByKeyAndFields(
                    this,
                    dateKey = item.srcDateKey,
                    timeLabel = item.timeLabel,
                    category = item.title,
                    notes = item.description
                )
                if (removed) refreshLogs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
