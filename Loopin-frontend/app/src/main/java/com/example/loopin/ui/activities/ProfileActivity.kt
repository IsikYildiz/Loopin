package com.example.loopin.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log // <-- LOG İÇİN IMPORT EKLENDİ
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityProfileBinding
import com.example.loopin.models.Event
import com.example.loopin.models.UserProfile
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var currentUserProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
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
            Toast.makeText(this, "Could not find user", Toast.LENGTH_LONG).show()
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
                        loadUpcomingEvents(userId)
                    } else {
                        Log.e("DEBUG_LOOPIN", "[ProfileActivity] Sunucu 'success: false' dedi. Mesaj: ${profileResponse.message}")
                        Toast.makeText(this@ProfileActivity, profileResponse.message ?: "Couldn't get profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("DEBUG_LOOPIN", "[ProfileActivity] Sunucudan hata kodu alındı: ${response.code()}")
                    Toast.makeText(this@ProfileActivity, "There was a problem: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LOOPIN", "[ProfileActivity] Profil çekilirken Exception oluştu.", e)
                Toast.makeText(this@ProfileActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(profile: UserProfile) {

        binding.textFullName.text = profile.fullName
        binding.textUsername.text = "@${profile.username}"
        binding.textEmail.text = profile.email
        binding.textBio.text = profile.bio.takeIf { !it.isNullOrBlank() } ?: "No data"
        binding.textLocation.text = profile.location.takeIf { !it.isNullOrBlank() } ?: "No data"
        binding.textPhoneNumber.text = profile.phoneNumber.takeIf { !it.isNullOrBlank() } ?: "No data"
    }
    private fun loadUpcomingEvents(userId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.eventApi.getEventsUserParticipates(userId)
                if (response.isSuccessful && response.body() != null) {
                    val eventsResponse = response.body()!!
                    if (eventsResponse.success) {
                        val events= eventsResponse.events.take(3)
                        updateEventsUI(events)
                    }
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LOOPIN", "[ProfileActivity] Etkinlikler çekilirken Exception.", e)
            }
        }
    }

    private fun updateEventsUI(events: List<Event>) {
        binding.cardUpcomingEvents.visibility = View.VISIBLE
        binding.containerEvents.removeAllViews() // Önceki veriyi temizle

        if (events.isEmpty()) {
            binding.textNoEvents.visibility = View.VISIBLE
        } else {
            binding.textNoEvents.visibility = View.GONE
            events.forEach { event ->
                val textView = TextView(this).apply {
                    text = event.eventName
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@ProfileActivity, android.R.color.white))
                    background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.event_item_background)
                    setPadding(24, 24, 24, 24) // padding değerlerini ayarlayabilirsiniz
                    textAlignment = View.TEXT_ALIGNMENT_CENTER

                    // TextView'ler arasına boşluk koymak için
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 16 // piksel cinsinden boşluk
                    }
                    layoutParams = params
                }
                binding.containerEvents.addView(textView)
            }
        }
    }
}