package com.example.ap2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rowApiKey.root.setOnClickListener { Toast.makeText(this, "API Key", Toast.LENGTH_SHORT).show() }
        binding.rowBackup.root.setOnClickListener { Toast.makeText(this, "Backup & Export", Toast.LENGTH_SHORT).show() }
        binding.rowAbout.root.setOnClickListener { Toast.makeText(this, "About", Toast.LENGTH_SHORT).show() }

        binding.tvVersion.text = "Version 1.0.0"

        setupBottomNav()
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



