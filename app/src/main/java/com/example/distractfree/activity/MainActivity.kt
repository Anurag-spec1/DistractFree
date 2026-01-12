package com.example.distractfree.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.distractfree.services.FocusAccessibilityService
import com.example.distractfree.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore toggle
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        binding.switchInstagram.isChecked = prefs.getBoolean("block_instagram_reels", false)
        binding.switchYoutube.isChecked=prefs.getBoolean("block_youtube_shorts",false)
        binding.switchPorn.isChecked=prefs.getBoolean("block_explicit_words",false)

        binding.switchInstagram.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("block_instagram_reels", isChecked).apply()
        }

        binding.switchYoutube.setOnCheckedChangeListener {_, isChecked ->
            prefs.edit().putBoolean("block_youtube_shorts",isChecked).apply()
        }

        binding.switchPorn.setOnCheckedChangeListener { _,isChecked ->
            prefs.edit().putBoolean("block_explicit_words",isChecked).apply()
        }
        // Prompt for permission
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${FocusAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(service) == true
    }
}