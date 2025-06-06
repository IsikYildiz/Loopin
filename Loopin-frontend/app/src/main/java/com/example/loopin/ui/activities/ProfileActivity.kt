package com.example.loopin.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log // <-- LOG İÇİN IMPORT EKLENDİ
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityProfileBinding
import com.example.loopin.models.UserProfile
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var currentUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.init(applicationContext)

        binding.buttonEditProfile.setOnClickListener {
            currentUserProfile?.let { profile ->
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("USER_PROFILE_DATA", profile)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = PreferenceManager.getUserId()
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Kullanıcı bulunamadı! Lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        Log.d("DEBUG_LOOPIN", "[ProfileActivity] Profil bilgileri userId: $userId için çekiliyor...")

        lifecycleScope.launch {
            try {
                val response = ApiClient.userApi.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val profileResponse = response.body()!!
                    if (profileResponse.success && profileResponse.user != null) {

                        // BİZE SUNUCUDAN NE GELDİĞİNİ SÖYLE
                        Log.d("DEBUG_LOOPIN", "[ProfileActivity] Sunucudan gelen veri: ${profileResponse.user}")

                        updateUI(profileResponse.user)
                        currentUserProfile = profileResponse.user
                    } else {
                        Log.e("DEBUG_LOOPIN", "[ProfileActivity] Sunucu 'success: false' dedi. Mesaj: ${profileResponse.message}")
                        Toast.makeText(this@ProfileActivity, profileResponse.message ?: "Profil bilgileri alınamadı", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("DEBUG_LOOPIN", "[ProfileActivity] Sunucudan hata kodu alındı: ${response.code()}")
                    Toast.makeText(this@ProfileActivity, "Bir hata oluştu: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LOOPIN", "[ProfileActivity] Profil çekilirken Exception oluştu.", e)
                Toast.makeText(this@ProfileActivity, "Ağ hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(profile: UserProfile) {
        if (!profile.profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile.profileImage)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageProfile)
        } else {
            binding.imageProfile.setImageResource(R.drawable.ic_person)
        }

        binding.textFullName.text = profile.fullName
        binding.textUsername.text = "@${profile.username}"
        binding.textEmail.text = profile.email
        binding.textBio.text = profile.bio.takeIf { !it.isNullOrBlank() } ?: "Eklenmemiş"
        binding.textLocation.text = profile.location.takeIf { !it.isNullOrBlank() } ?: "Eklenmemiş"
        binding.textPhoneNumber.text = profile.phoneNumber.takeIf { !it.isNullOrBlank() } ?: "Eklenmemiş"
    }
}