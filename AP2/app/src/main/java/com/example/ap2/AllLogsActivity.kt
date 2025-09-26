package com.example.ap2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.databinding.ActivityAllLogsBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AllLogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllLogsBinding
    private lateinit var adapter: LogItemAdapter
    private val displayDateFmt = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())

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
        adapter = LogItemAdapter(emptyList())
        binding.recyclerLogs.adapter = adapter
    }

    private fun refreshLogs() {
        val all = ActivityRepository.getAll(this)
        val items = all.map { entry ->
            LogItem(
                iconResId = R.drawable.ic_placeholder,
                title = entry.category,
                description = entry.notes,
                date = displayDateFmt.format(entry.date)
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

        binding.btnFilter.setOnClickListener {
            // TODO: Implement filter dialog
        }
    }
}
