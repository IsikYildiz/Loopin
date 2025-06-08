package com.example.loopin.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder // Geocoding için
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityMainBinding
import com.example.loopin.models.UpdateProfileRequest
import com.example.loopin.network.ApiClient
import com.example.loopin.ui.activities.notifications.NotificationsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.IOException // Geocoder için
import java.util.Locale // Geocoder için

/*Uygulamanın en önemli kısmı; home, chats, events ve calendar fragmentlerini içerir.
Aynı zamanda sayfanın üstündeki toolbardan, profil ve bildirim aktivitelerine gidilir ve
arama çubuğu ile profiller ya da etkinlikler aranabilir.*/
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Konum izni için ActivityResultLauncher
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("LocationPermission", "Permission granted from launcher")
                checkAndPromptForLocationUpdate() // İzin verildiyse tekrar kontrol et
            } else {
                Log.d("LocationPermission", "Permission denied from launcher")
                // Kullanıcıya iznin neden gerekli olduğunu açıklayan bir mesaj gösterebilirsiniz.
                // Örneğin: Toast.makeText(this, "Konum izni verilmedi.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val userId = PreferenceManager.getUserId()
        Log.d("MainActivityUser", "User ID: $userId") // userId'yi loglayalım

        // --- Navigasyon ve Butonlar ---
        setupNavigationAndButtons()

        // --- Konum Kontrolü ve Güncelleme ---
        // Eğer kullanıcı ID'si varsa ve giriş yeni yapıldıysa veya belirli bir koşulda çağırın
        if (userId != null) {
            checkAndPromptForLocationUpdate()
        }
    }

    private fun setupNavigationAndButtons() {
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
        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        navView.itemIconTintList = null

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_chats,
                R.id.navigation_events,
                R.id.navigation_calendar
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // --- KONUM İLE İLGİLİ FONKSİYONLAR ---

    private fun checkAndPromptForLocationUpdate() {
        lifecycleScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId == null) {
                Log.e("LocationUpdate", "User ID not found in checkAndPrompt.")
                return@launch
            }

            val response= ApiClient.userApi.getUserProfile(userId)
            val userLocationFromDb: String? = response.body()?.user?.location
            Log.d("LocationUpdate", "User location from DB for $userId: $userLocationFromDb")


            if (userLocationFromDb.isNullOrEmpty()) {
                Log.d("LocationUpdate", "User location not found in DB. Checking device location.")
                handleDeviceLocationCheck()
            } else {
                Log.d("LocationUpdate", "User location already exists in DB: $userLocationFromDb")
            }
        }
    }

    private fun handleDeviceLocationCheck() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (isLocationEnabled()) {
                    Log.d("LocationUpdate", "Permission granted & location enabled. Fetching location...")
                    fetchDeviceLocation()
                } else {
                    Log.d("LocationUpdate", "Permission granted but location services disabled.")
                    promptToEnableLocationServices()
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Log.d("LocationUpdate", "Showing rationale for location permission.")
                showPermissionRationaleDialog()
            }
            else -> {
                Log.d("LocationUpdate", "Requesting location permission.")
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptToEnableLocationServices() {
        AlertDialog.Builder(this)
            .setMessage(R.string.location_services_disabled_prompt)
            .setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_needed_title)
            .setMessage(R.string.location_permission_rationale)
            .setPositiveButton(R.string.ok) { _, _ ->
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationUpdate", "fetchDeviceLocation called without permission.")
            // Belki kullanıcıya bir mesaj gösterilebilir veya izin tekrar istenebilir.
            // requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("LocationUpdate", "Device location fetched: Lat $latitude, Lon $longitude")

                    // Geocoding işlemini bir coroutine içinde yapalım
                    lifecycleScope.launch {
                        var addressText = "Lat: $latitude, Lon: $longitude" // Default text
                        try {
                            // Geocoding IO işlemi olduğu için Dispatchers.IO kullanın
                            val geocoder = Geocoder(this@MainActivity, Locale.ENGLISH) // Locale.ENGLISH olarak güncellendi
                            // API 33 ve üzeri için listener ile kullanmak daha doğru olsa da,
                            // coroutine içinde Dispatchers.IO ile de çalışacaktır.
                            // Alternatif olarak SDK versiyonuna göre listener kullanabilirsiniz.
                            @Suppress("DEPRECATION") // Eğer minSdk < 33 ise bu satır gerekebilir.
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                            if (!addresses.isNullOrEmpty()) {
                                addresses[0]?.let { foundAddress ->
                                    // Sadece şehir ve ülke gibi daha genel bir bilgi almak isteyebilirsiniz.
                                    // Veya tam adresi kullanabilirsiniz: foundAddress.getAddressLine(0)
                                    val city = foundAddress.locality
                                    val country = foundAddress.countryName
                                    addressText = if (city != null && country != null) {
                                        "$city, $country"
                                    } else {
                                        foundAddress.getAddressLine(0) ?: addressText
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            Log.e("LocationUpdate", "Geocoder service not available or I/O error", e)
                            // addressText varsayılan değerini (Lat/Lon) koruyacak
                        } catch (e: IllegalArgumentException) {
                            Log.e("LocationUpdate", "Geocoder illegal argument (invalid lat/lon)", e)
                            // addressText varsayılan değerini (Lat/Lon) koruyacak
                        } catch (e: Exception) { // Diğer olası hatalar için
                            Log.e("LocationUpdate", "Geocoder failed with unknown error", e)
                        }
                        showLocationUpdatePopup(addressText, latitude, longitude)
                    }
                } else {
                    Log.w("LocationUpdate", "Failed to get location: location is null.")
                    Toast.makeText(this, getString(R.string.location_not_retrieved), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LocationUpdate", "Failed to get location", exception)
                Toast.makeText(this, getString(R.string.location_error, exception.message), Toast.LENGTH_LONG).show()
            }
    }

    private fun showLocationUpdatePopup(locationString: String, latitude: Double, longitude: Double) {
        val displayMessage = getString(R.string.update_location_prompt, locationString)
        AlertDialog.Builder(this)
            .setTitle(R.string.update_location_title)
            .setMessage(displayMessage)
            .setPositiveButton(R.string.yes) { _, _ ->
                Log.d("LocationUpdate", "User accepted location update.")
                updateUserLocationInApi(latitude, longitude, locationString)
            }
            .setNegativeButton(R.string.no, null)
            .setCancelable(false)
            .show()
    }

    private fun updateUserLocationInApi(latitude: Double, longitude: Double, locationAddress: String?) {
        lifecycleScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId == null) {
                Log.e("LocationUpdate", "Cannot update API location, User ID not found.")
                return@launch
            }
            Log.d("LocationUpdate", "Updating API location for user $userId to: $locationAddress (Lat: $latitude, Lon: $longitude)")

            try{
                val updateProfileRequest = UpdateProfileRequest(userId, location = locationAddress)
                val response = ApiClient.userApi.updateProfile(updateProfileRequest)
                if (response.isSuccessful) {
                    Log.d("LocationUpdate", "Location updated successfully in API.")
                    Toast.makeText(this@MainActivity, "Your location is updated", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("LocationUpdate", "Failed to update location in API: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "Encountered a problem while updating your location", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception){
                Log.e("LocationUpdate", "Error updating location in API", e)
                Toast.makeText(this@MainActivity, "Encountered a problem while updating your location", Toast.LENGTH_SHORT).show()
            }
            Log.w("LocationAPI", "updateUserLocationInApi is a dummy.")
        }
    }
}