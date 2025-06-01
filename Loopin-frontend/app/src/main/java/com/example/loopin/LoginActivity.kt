package com.example.loopin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loopin.databinding.ActivityLoginBinding

//Kullanıcı giriş işlemi için kullanılır.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Şimdilik tuşa tıklandığı gibi ana aktiviteye geçiş yapıyoruz
        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //Kayıt ol tuşuna tıklandığında SignupActivity'ye geçiş yapıyoruz
        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}