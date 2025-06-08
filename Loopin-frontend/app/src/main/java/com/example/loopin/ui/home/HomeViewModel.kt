package com.example.loopin.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.Event
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // Etkinlik listesini tutacak LiveData
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    // Yükleme durumunu (ProgressBar) kontrol etmek için LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Hata mesajını (TextView) göstermek için LiveData
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // **DEĞİŞİKLİK BURADA:** Fonksiyon adını ve API çağrısını düzelttik.
    fun fetchPublicEvents() {
        viewModelScope.launch {
            _isLoading.value = true // Yükleme başladı
            _error.value = null   // Eski hataları temizle
            try {
                val userId = PreferenceManager.getUserId()
                val response = userId?.let { ApiClient.eventApi.getPublicEvents(it) }
                if (response != null) {
                    if (response.isSuccessful && response.body() != null) {
                        _events.value = response.body()!!.events
                    } else {
                        _error.value = "Etkinlikler yüklenemedi."
                        Log.e("HomeViewModel", "Error fetching events: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Bir ağ hatası oluştu."
                Log.e("HomeViewModel", "Exception fetching events", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}