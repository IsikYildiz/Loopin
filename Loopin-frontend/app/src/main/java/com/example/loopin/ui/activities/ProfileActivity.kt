package com.example.loopin.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loopin.databinding.ActivityProfileBinding

//Kullanıcıların profilini görüntülemek için kullanılır.
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}