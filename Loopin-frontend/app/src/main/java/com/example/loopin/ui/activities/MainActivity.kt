package com.example.loopin.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityMainBinding

/*Uygulamanın en önemli kısmı; home, chats, events ve calendar fragmentlerini içerir.
Aynı zamanda sayfanın üstündeki toolbardan, profil ve bildirim aktivitelerine gidilir ve
arama çubuğu ile profiller ya da etkinlikler aranabilir.*/
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = PreferenceManager.getUserId()
        println(userId)


        //Arama çubuğunun fonksiyonu sonradan eklenecek
        val searchView = findViewById<SearchView>(R.id.search_view)
        val buttonNotifications = findViewById<ImageButton>(R.id.buttonNotifications)
        val buttonProfile = findViewById<ImageButton>(R.id.buttonProfile)

        buttonNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val navView: BottomNavigationView = binding.navView
        navView.itemIconTintList=null

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chats,
                R.id.navigation_chats,
                R.id.navigation_events,
                R.id.navigation_calendar
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}