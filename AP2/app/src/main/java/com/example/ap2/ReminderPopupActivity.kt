package com.example.ap2

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.databinding.ActivityReminderPopupBinding

class ReminderPopupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderPopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReminderPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure the popup can appear over lockscreen and turn screen on for urgent reminders
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val title = intent.getStringExtra("title") ?: "Reminder"
        val content = intent.getStringExtra("content") ?: ""
        binding.tvPopupTitle.text = title
        binding.tvPopupContent.text = content

        binding.btnDismiss.setOnClickListener { finish() }
    }
}
