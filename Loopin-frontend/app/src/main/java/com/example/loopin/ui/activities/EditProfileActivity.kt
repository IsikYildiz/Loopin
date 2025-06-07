package com.example.loopin.ui.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityEditProfileBinding
import com.example.loopin.models.ChangePasswordRequest
import com.example.loopin.models.UpdateProfileRequest
import com.example.loopin.models.UserProfile
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var currentUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.init(applicationContext)

        // Veriyi al...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentUserProfile = intent.getSerializableExtra("USER_PROFILE_DATA", UserProfile::class.java)
        } else {
            @Suppress("DEPRECATION")
            currentUserProfile = intent.getSerializableExtra("USER_PROFILE_DATA") as? UserProfile
        }

        if (currentUserProfile == null) {
            Toast.makeText(this, "Kullanıcı verisi yüklenemedi!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        populateFields()

        binding.buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateFields() {
        currentUserProfile?.let { profile ->
            binding.editFullName.setText(profile.fullName)
            binding.editUsername.setText(profile.username)
            binding.editEmail.setText(profile.email)
            binding.editBio.setText(profile.bio)
            binding.editLocation.setText(profile.location)
            binding.editPhoneNumber.setText(profile.phoneNumber)
        }
    }

    private fun saveChanges() {
        val userId = PreferenceManager.getUserId()
        if(userId == null || userId == -1) {
            Toast.makeText(this, "Kullanıcı kimliği doğrulanamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPassword = binding.editCurrentPassword.text.toString()
        val newPassword = binding.editNewPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()

        val isPasswordChangeRequested = currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()

        if (isPasswordChangeRequested) {
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Şifre değiştirmek için mevcut şifrenizi girin.", Toast.LENGTH_LONG).show()
                return
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "Yeni şifre en az 6 karakter olmalı.", Toast.LENGTH_LONG).show()
                return
            }
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Yeni şifreler eşleşmiyor.", Toast.LENGTH_LONG).show()
                return
            }
        }

        showLoading(true)

        updateProfileInfo(userId, isPasswordChangeRequested)
    }

    private fun updateProfileInfo(userId: Int, passwordChangeRequested: Boolean) {
        val updateRequest = UpdateProfileRequest(
            userId = userId,
            fullName = binding.editFullName.text.toString().trim(),
            username = binding.editUsername.text.toString().trim(),
            email = binding.editEmail.text.toString().trim(),
            bio = binding.editBio.text.toString().trim(),
            location = binding.editLocation.text.toString().trim(),
            phoneNumber = binding.editPhoneNumber.text.toString().trim()
        )

        // BİZE NE GÖNDERDİĞİNİ SÖYLE
        Log.d("DEBUG_LOOPIN", "[EditProfile] Sunucuya gönderilen veri: $updateRequest")

        lifecycleScope.launch {
            try {
                val response = ApiClient.userApi.updateProfile(updateRequest)

                // SUNUCUNUN NE CEVAP VERDİĞİNİ SÖYLE
                Log.d("DEBUG_LOOPIN", "[EditProfile] Sunucudan gelen cevap kodu: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DEBUG_LOOPIN", "[EditProfile] Profil güncelleme BAŞARILI.")
                    if (passwordChangeRequested) {
                        updatePassword(userId)
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Profil başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.body()?.message ?: "Profil bilgileri güncellenemedi."
                    // SUNUCU NEDEN BAŞARISIZ OLDUĞUNU SÖYLESİN
                    Log.e("DEBUG_LOOPIN", "[EditProfile] Profil güncelleme hatası: ${response.code()} - $errorBody")
                    Toast.makeText(this@EditProfileActivity, "Hata: $errorBody", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            } catch (e: Exception) {
                // GENEL BİR HATA OLURSA SÖYLESİN
                Log.e("DEBUG_LOOPIN", "[EditProfile] Profil güncellemede Exception oluştu.", e)
                Toast.makeText(this@EditProfileActivity, "Ağ hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        }
    }

    private fun updatePassword(userId: Int) {
        val passwordRequest = ChangePasswordRequest(
            userId = userId,
            currentPassword = binding.editCurrentPassword.text.toString(),
            newPassword = binding.editNewPassword.text.toString()
        )

        Log.d("DEBUG_LOOPIN", "[EditProfile] Şifre değişikliği isteniyor.")

        lifecycleScope.launch {
            try {
                val response = ApiClient.userApi.changePassword(passwordRequest)

                Log.d("DEBUG_LOOPIN", "[EditProfile] Şifre için sunucu cevabı: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DEBUG_LOOPIN", "[EditProfile] Şifre başarıyla güncellendi.")
                    Toast.makeText(this@EditProfileActivity, "Profil ve şifre başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.body()?.message ?: "Mevcut şifreniz yanlış olabilir."
                    Log.e("DEBUG_LOOPIN", "[EditProfile] Şifre güncelleme hatası: ${response.code()} - $errorBody")
                    Toast.makeText(this@EditProfileActivity, "Şifre güncellenemedi: $errorBody", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LOOPIN", "[EditProfile] Şifre güncelleme exception: ", e)
                Toast.makeText(this@EditProfileActivity, "Ağ hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.saveProgressBar.visibility = View.VISIBLE
            binding.buttonSave.isEnabled = false
        } else {
            binding.saveProgressBar.visibility = View.GONE
            binding.buttonSave.isEnabled = true
        }
    }
}