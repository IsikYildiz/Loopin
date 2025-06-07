package com.example.loopin.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.Event
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {

    // DEĞİŞTİ: Artık veriyi doğrudan Repository'den almıyoruz.
    // Bunun yerine, API'den gelen veriyi tutacak özel bir LiveData oluşturuyoruz.
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    // YENİ FONKSİYON: Giriş yapmış kullanıcının oluşturduğu etkinlikleri API'den çeker.
    fun fetchMyCreatedEvents() {
        // Önce SharedPreferences'dan kullanıcı ID'sini alıyoruz.
        val userId = PreferenceManager.getUserId()

        // Eğer kullanıcı ID'si yoksa (kullanıcı giriş yapmamışsa) işlemi durdur.
        if (userId == null || userId == -1) {
            Log.e("EventsViewModel", "Kullanıcı ID bulunamadı. Etkinlikler çekilemiyor.")
            return
        }

        // Ağ isteğini arayüzü bloklamadan, arka planda yapmak için viewModelScope kullanıyoruz.
        viewModelScope.launch {
            try {
                // ApiClient aracılığıyla sunucudaki doğru endpoint'e istek atıyoruz.
                val response = ApiClient.eventApi.getEventsUserParticipates(userId)
                val responseUpcoming = ApiClient.eventApi.getUpcomingEventsUserParticipates(userId)

                // Eğer istek başarılı olduysa ve sunucudan gelen cevap olumluysa...
                if (response.isSuccessful && response.body() != null && responseUpcoming.isSuccessful && responseUpcoming.body() != null) {
                    // Gelen etkinlik listesini _events LiveData'sına gönderiyoruz.
                    // Bu sayede arayüz (Fragment) otomatik olarak güncellenecek.
                    val events = response.body()!!.events + responseUpcoming.body()!!.events
                    _events.value = events
                } else {
                    // İstek başarısız olursa Logcat'e hata basıyoruz.
                    Log.e("EventsViewModel", "Etkinlikler çekilemedi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Ağ hatası gibi bir istisna oluşursa Logcat'e hata basıyoruz.
                Log.e("EventsViewModel", "Etkinlikler çekilirken bir istisna oluştu", e)
            }
        }
    }
}