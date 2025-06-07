package com.example.loopin.ui.activities

import android.os.Build
import android.os.Bundle
import android.util.Log // <-- EKSİK OLAN IMPORT SATIRI EKLENDİ
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityEditProfileBinding
import com.example.loopin.models.UpdateProfileRequest
import com.example.loopin.models.UserProfile
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var currentUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.init(applicationContext)

        // Veriyi al
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

        // Alanları doldur
        populateFields()

        // Kaydet butonuna basıldığında
        binding.buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateFields() {
        currentUserProfile?.let { profile ->
            binding.editFullName.setText(profile.fullName)
            binding.editUsername.setText(profile.username)
            binding.editBio.setText(profile.bio)
            binding.editLocation.setText(profile.location)
            binding.editPhoneNumber.setText(profile.phoneNumber)
        }
    }

    private fun saveChanges() {
        binding.saveProgressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = false
        binding.buttonSave.text = ""

        // Yeni verileri EditText'lerden oku
        val newFullName = binding.editFullName.text.toString().trim()
        val newUsername = binding.editUsername.text.toString().trim()
        val newBio = binding.editBio.text.toString().trim()
        val newLocation = binding.editLocation.text.toString().trim()
        val newPhoneNumber = binding.editPhoneNumber.text.toString().trim()

        val userId = PreferenceManager.getUserId()

        if(userId == null || userId == -1) {
            Toast.makeText(this, "Kullanıcı kimliği doğrulanamadı!", Toast.LENGTH_SHORT).show()
            resetButton()
            return
        }

        // Güncellenmiş istek nesnesini oluştur
        val updateRequest = UpdateProfileRequest(
            userId = userId,
            fullName = newFullName,
            username = newUsername,
            bio = newBio,
            location = newLocation,
            phoneNumber = newPhoneNumber
        )

        Log.d("UpdateProfile", "İstek Gönderiliyor: $updateRequest")

        lifecycleScope.launch {
            try {
                val response = ApiClient.userApi.updateProfile(updateRequest)

                Log.d("UpdateProfile", "Sunucu Cevabı Kodu: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val updateResponse = response.body()!!
                    Log.d("UpdateProfile", "Başarılı Cevap Body: $updateResponse")
                    if (updateResponse.success) {
                        Toast.makeText(this@EditProfileActivity, "Profil başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, updateResponse.message ?: "Güncelleme başarısız.", Toast.LENGTH_SHORT).show()
                        resetButton()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateProfile", "HATA CEVABI: ${response.code()} - $errorBody")
                    Toast.makeText(this@EditProfileActivity, "Hata: ${response.code()}", Toast.LENGTH_SHORT).show()
                    resetButton()
                }

            } catch (e: Exception) {
                Log.e("UpdateProfile", "İstek Sırasında Exception Oluştu", e)
                Toast.makeText(this@EditProfileActivity, "Ağ hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
        }
    }

    private fun resetButton() {
        binding.saveProgressBar.visibility = View.GONE
        binding.buttonSave.isEnabled = true
        binding.buttonSave.text = "KAYDET"
    }
}