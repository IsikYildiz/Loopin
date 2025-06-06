package com.example.loopin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.loopin.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import java.io.IOException
import com.example.loopin.models.RegisterRequest
import com.example.loopin.models.RegisterResponse
import com.example.loopin.models.CheckUsernameRequest
import com.example.loopin.models.CheckEmailRequest
import com.example.loopin.network.ApiClient

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollViewContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, insets.top, view.paddingRight, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            var isValid = true

            if (username.isEmpty()) {
                binding.tilUsername.error = "Username cannot be empty"
                isValid = false
            } else {
                binding.tilUsername.error = null
            }

            if (fullName.isEmpty()) {
                binding.tilFullName.error = "Full Name cannot be empty"
                isValid = false
            } else {
                binding.tilFullName.error = null
            }

            if (email.isEmpty()) {
                binding.tilEmail.error = "E-mail cannot be empty"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid e-mail address"
                isValid = false
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password cannot be empty"
                isValid = false
            } else if (password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters long"
                isValid = false
            } else {
                binding.tilPassword.error = null
            }

            if (isValid) {
                checkUsernameAndEmail(username, email) { usernameExists, emailExists ->
                    if (usernameExists) {
                        binding.tilUsername.error = "Username already exists"
                        Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else if (emailExists) {
                        binding.tilEmail.error = "Email already registered"
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        proceedWithRegistration(fullName, username, email, password)
                    }
                }
            }
        }
    }

    private fun checkUsernameAndEmail(
        username: String,
        email: String,
        callback: (Boolean, Boolean) -> Unit
    ) {
        lifecycleScope.launch {
            try {
                val usernameResponse = ApiClient.userApi.checkUsername(CheckUsernameRequest(username))
                val emailResponse = ApiClient.userApi.checkEmail(CheckEmailRequest(email))

                val usernameExists = usernameResponse.isSuccessful && usernameResponse.body()?.exists == true
                val emailExists = emailResponse.isSuccessful && emailResponse.body()?.exists == true

                callback(usernameExists, emailExists)
            } catch (e: IOException) {
                Toast.makeText(this@SignupActivity, "Network error: Please check your connection", Toast.LENGTH_LONG).show()
                Log.e("SignUpActivityTAG", "Network Error", e)
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("SignUpActivityTAG", "Unexpected Error", e)
            }
        }
    }

    private fun proceedWithRegistration(
        fullName: String,
        username: String,
        email: String,
        password: String
    ) {
        lifecycleScope.launch {
            try {
                val signUpRequest = RegisterRequest(fullName, username, email, password)
                val response = ApiClient.userApi.register(signUpRequest)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null && registerResponse.success) {
                        Toast.makeText(
                            this@SignupActivity,
                            "Registration Successful! User ID: ${registerResponse.userId}",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val errorMessage = registerResponse?.error ?: "Registration failed: Unknown server error"
                        Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Registration Error: ${response.code()}\n${errorBody ?: "No details"}"
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@SignupActivity, "Network error: Please check your connection", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}