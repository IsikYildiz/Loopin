package com.example.loopin.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loopin.databinding.ActivityEventBinding

//Tek bir etkinlik göstermek için kullanılır.
class EventActivity : AppCompatActivity(){
    private lateinit var binding: ActivityEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}