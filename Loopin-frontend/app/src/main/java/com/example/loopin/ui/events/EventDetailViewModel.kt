package com.example.loopin.ui.events // veya viewmodels paketiniz

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.DeleteEventRequest
import com.example.loopin.models.Event
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {

    // Detayları gelen etkinliği tutacak LiveData
    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> = _event

    // Silme işleminin sonucunu bildirecek LiveData
    private val _eventDeletedStatus = MutableLiveData<Boolean>()
    val eventDeletedStatus: LiveData<Boolean> = _eventDeletedStatus

    // Verilen ID ile tek bir etkinliği API'den çeker
    fun fetchEventDetails(eventId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.eventApi.getEventById(eventId)
                if (response.isSuccessful) {
                    _event.value = response.body()?.event
                } else {
                    _event.value = null
                    Log.e("EventDetailViewModel", "Detay çekilemedi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _event.value = null
                Log.e("EventDetailViewModel", "Detay çekilirken istisna oluştu", e)
            }
        }
    }

    // Verilen ID ile etkinliği silmek için API'ye istek atar
    fun deleteEvent(eventId: Int) {
        val userId = PreferenceManager.getUserId()
        if (userId == null || userId == -1) {
            _eventDeletedStatus.value = false
            Log.e("EventDetailViewModel", "Silme başarısız: Kullanıcı ID bulunamadı.")
            return
        }

        viewModelScope.launch {
            try {
                // API, silme işlemi için de kimin istek attığını bilmek isteyebilir.
                // Bu yüzden bir DeleteEventRequest oluşturuyoruz.
                val request = DeleteEventRequest(userId = userId)
                val response = ApiClient.eventApi.deleteEvent(eventId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _eventDeletedStatus.value = true
                } else {
                    _eventDeletedStatus.value = false
                    Log.e("EventDetailViewModel", "Silme başarısız: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _eventDeletedStatus.value = false
                Log.e("EventDetailViewModel", "Silme sırasında istisna oluştu", e)
            }
        }
    }
}