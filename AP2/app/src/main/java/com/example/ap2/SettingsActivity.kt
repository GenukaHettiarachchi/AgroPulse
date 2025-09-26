package com.example.ap2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivitySettingsBinding
import com.example.ap2.databinding.RowSettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set titles and subtitles from layout tags in the form "Title|Subtitle"
        applyRowTextsFromTag(binding.rowApiKey)
        applyRowTextsFromTag(binding.rowBackup)
        applyRowTextsFromTag(binding.rowAbout)

        // Explicitly set the texts to guarantee correct display
        binding.rowApiKey.tvTitle.text = "API Key"
        binding.rowApiKey.tvSubtitle.text = "Manage your API credentials"

        binding.rowBackup.tvTitle.text = "Backup and Export"
        binding.rowBackup.tvSubtitle.text = "Sync and export your data"

        binding.rowAbout.tvTitle.text = "About"
        binding.rowAbout.tvSubtitle.text = "App information and support"

        binding.rowApiKey.root.setOnClickListener { Toast.makeText(this, "API Key", Toast.LENGTH_SHORT).show() }
        binding.rowBackup.root.setOnClickListener { Toast.makeText(this, "Backup & Export", Toast.LENGTH_SHORT).show() }
        binding.rowAbout.root.setOnClickListener { Toast.makeText(this, "About", Toast.LENGTH_SHORT).show() }

        binding.tvVersion.text = "Version 1.0.0"

        setupBottomNav()
    }

    private fun applyRowTextsFromTag(row: RowSettingsBinding) {
        val tagValue = row.root.tag?.toString() ?: return
        val parts = tagValue.split('|')
        if (parts.isNotEmpty()) {
            row.tvTitle.text = parts[0].trim()
        }
        if (parts.size > 1) {
            row.tvSubtitle.text = parts[1].trim()
        }
    }

    private fun setupBottomNav() {
        val bottom = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_settings
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { startActivity(android.content.Intent(this, MainActivity::class.java)); true }
                R.id.nav_calendar -> { startActivity(android.content.Intent(this, CalendarActivity::class.java)); true }
                R.id.nav_logs -> { startActivity(android.content.Intent(this, AllLogsActivity::class.java)); true }
                R.id.nav_reminders -> { startActivity(android.content.Intent(this, RemindersActivity::class.java)); true }
                R.id.nav_settings -> true
                else -> true
            }
        }
    }
}



