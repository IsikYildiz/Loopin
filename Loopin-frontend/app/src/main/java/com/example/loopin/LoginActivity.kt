package com.example.loopin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopin.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.io.IOException
import com.example.loopin.models.LoginRequest
import com.example.loopin.models.LoginResponse
import com.example.loopin.network.ApiClient
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty()) {
                binding.textInputLayoutEmail.error = "Email required"
            } else {
                binding.textInputLayoutEmail.error = null
            }

            if (password.isEmpty()) {
                binding.textInputLayoutPassword.error = "Password required"
            } else {
                binding.textInputLayoutPassword.error = null
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.userApi.login(LoginRequest(email, password))

                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null && loginResponse.success) {
                                // Bunun sonradan gösterilmesine gerek yok
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Successful! User: ${loginResponse.user?.username}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                val errorMessage = loginResponse?.message ?: "Login failed: Unknown server response"
                                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            var apiErrorMessage = "Login Error: ${response.code()}"
                            if (!errorBody.isNullOrEmpty()) {
                                try {
                                    val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
                                    apiErrorMessage = errorResponse?.message ?: errorResponse?.error ?: apiErrorMessage
                                } catch (e: Exception) {
                                    Log.e("LoginActivityTAG", "Error body parsing failed", e)
                                }
                            }
                            Toast.makeText(this@LoginActivity, apiErrorMessage, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(this@LoginActivity, "Network error: Please check your connection", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}