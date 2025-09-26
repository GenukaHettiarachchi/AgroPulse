package com.example.ap2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivityPermissionsBinding
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class PermissionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionsBinding

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
        goNext()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSkipAll.setOnClickListener { goNext() }
        binding.btnStorageSkip.setOnClickListener { /* skip storage */ }
        binding.btnStorageAllow.setOnClickListener { /* request storage */ }
        binding.btnNotifSkip.setOnClickListener { goNext() }
        binding.btnNotifAllow.setOnClickListener { requestNotificationsPermission() }
    }

    private fun goNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            // Permission not required below Android 13
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            goNext()
            return
        }
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            Toast.makeText(this, "Notifications already enabled", Toast.LENGTH_SHORT).show()
            goNext()
        } else {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
