package com.example.loopin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loopin.databinding.ActivitySignupBinding

//Kullanıcı kayıt işlemi için kullanılır.
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}