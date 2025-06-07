package com.example.loopin.ui.calendar

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

object LocationGeocoder {

    suspend fun getCoordinatesFromLocationName(context: Context, locationName: String): Pair<Double, Double>? {
        if (!Geocoder.isPresent()) {
            Log.e("LocationGeocoder", "Geocoder service is not present on this device.")
            return null
        }

        // Geocoder her çağrıda context ile yeniden oluşturulur.
        val geocoder = Geocoder(context.applicationContext, Locale.getDefault()) // applicationContext kullanımı daha güvenli

        return try {
            val addresses: List<Address>? = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(locationName, 1)
                } catch (e: IOException) {
                    Log.e("LocationGeocoder", "Geocoder I/O error for '$locationName': ${e.message}", e)
                    null
                } catch (e: IllegalArgumentException) {
                    Log.e("LocationGeocoder", "Invalid location name provided: '$locationName'", e)
                    null
                }
            }

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                Pair(address.latitude, address.longitude)
            } else {
                Log.w("LocationGeocoder", "No address found for '$locationName'")
                null
            }
        } catch (e: Exception) {
            Log.e("LocationGeocoder", "Unexpected error during geocoding for '$locationName': ${e.message}", e)
            null
        }
    }
}