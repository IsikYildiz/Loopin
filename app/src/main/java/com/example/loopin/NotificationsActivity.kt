package com.example.loopin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loopin.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}