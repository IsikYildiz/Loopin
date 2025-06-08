package com.example.loopin.ui.events // veya viewmodels paketiniz

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.DeleteEventRequest
import com.example.loopin.models.Event
import com.example.loopin.models.JoinEventRequest
import com.example.loopin.models.LeaveEventRequest
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch
import com.example.loopin.models.Participant

// Kullanıcının etkinlikle olan ilişkisini temsil eden durumlar
sealed class UserEventStatus {
    object Loading : UserEventStatus()
    object Creator : UserEventStatus()
    object Participant : UserEventStatus()
    object NotParticipant : UserEventStatus()
    data class Error(val message: String) : UserEventStatus()
}

// Bir işlemin sonucunu (başarı/hata) bildirmek için kullanılacak sınıf
sealed class ActionStatus {
    data class Success(val message: String) : ActionStatus()
    data class Failure(val message: String) : ActionStatus()
}

class EventDetailViewModel : ViewModel() {

    // Detayları gelen etkinliği tutacak LiveData
    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> = _event

    // Katılımcı listesini tutacak LiveData
    private val _participants = MutableLiveData<List<Participant>>()
    val participants: LiveData<List<Participant>> = _participants

    // Silme işleminin sonucunu bildirecek LiveData
    private val _eventDeletedStatus = MutableLiveData<Boolean>()
    val eventDeletedStatus: LiveData<Boolean> = _eventDeletedStatus

    // YENİ: Kullanıcının etkinlikle olan durumunu tutacak LiveData
    private val _userStatus = MutableLiveData<UserEventStatus>()
    val userStatus: LiveData<UserEventStatus> = _userStatus

    // YENİ: Katılma/Ayrılma gibi aksiyonların sonucunu bildirecek LiveData
    private val _actionStatus = MutableLiveData<ActionStatus>()
    val actionStatus: LiveData<ActionStatus> = _actionStatus


    // YENİ ve GÜNCELLENMİŞ: Tüm etkinlik verilerini tek bir yerden yükleyen fonksiyon
    fun loadEventData(eventId: Int) {
        if (eventId == -1) {
            _userStatus.value = UserEventStatus.Error("Geçersiz Etkinlik ID'si")
            return
        }

        viewModelScope.launch {
            _userStatus.value = UserEventStatus.Loading
            try {
                val currentUserId = PreferenceManager.getUserId()
                if (currentUserId == null || currentUserId == -1) {
                    _userStatus.value = UserEventStatus.Error("Kullanıcı girişi bulunamadı.")
                    return@launch
                }

                // Eş zamanlı olarak etkinlik detaylarını ve katılımcıları çek
                val eventResponse = ApiClient.eventApi.getEventById(eventId)
                val participantsResponse = ApiClient.eventApi.getEventParticipants(eventId)

                if (eventResponse.isSuccessful && eventResponse.body() != null && participantsResponse.isSuccessful && participantsResponse.body() != null) {
                    val eventData = eventResponse.body()!!.event
                    val participantsData = participantsResponse.body()!!.participants

                    _event.value = eventData
                    _participants.value = participantsData

                    // Kullanıcı durumunu belirle
                    if (eventData?.creatorId == currentUserId) {
                        _userStatus.value = UserEventStatus.Creator
                    } else if (participantsData.any { it.id == currentUserId }) {
                        _userStatus.value = UserEventStatus.Participant
                    } else {
                        _userStatus.value = UserEventStatus.NotParticipant
                    }
                } else {
                    _userStatus.value = UserEventStatus.Error("Etkinlik verileri alınamadı.")
                    Log.e("EventDetailViewModel", "API Error: Event - ${eventResponse.errorBody()?.string()}, Participants - ${participantsResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _userStatus.value = UserEventStatus.Error("Bir ağ hatası oluştu.")
                Log.e("EventDetailViewModel", "Exception in loadEventData", e)
            }
        }
    }

    fun joinEvent(eventId: Int) {
        val userId = PreferenceManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val request = JoinEventRequest(eventId, userId)
                val response = ApiClient.eventApi.joinEvent(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _actionStatus.value = ActionStatus.Success("Etkinliğe katıldınız!")
                    loadEventData(eventId) // Durumu yenile
                } else {
                    _actionStatus.value = ActionStatus.Failure(response.body()?.error ?: "Katılma başarısız.")
                }
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.Failure("Bir hata oluştu.")
            }
        }
    }

    fun leaveEvent(eventId: Int) {
        val userId = PreferenceManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val request = LeaveEventRequest(eventId, userId)
                val response = ApiClient.eventApi.leaveEvent(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _actionStatus.value = ActionStatus.Success("Etkinlikten ayrıldınız.")
                    loadEventData(eventId) // Durumu yenile
                } else {
                    _actionStatus.value = ActionStatus.Failure(response.body()?.error ?: "Ayrılma başarısız.")
                }
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.Failure("Bir hata oluştu.")
            }
        }
    }


    fun deleteEvent(eventId: Int) {
        val userId = PreferenceManager.getUserId()
        if (userId == null || userId == -1) {
            _eventDeletedStatus.value = false
            Log.e("EventDetailViewModel", "Silme başarısız: Kullanıcı ID bulunamadı.")
            return
        }

        viewModelScope.launch {
            try {
                val request = DeleteEventRequest(userId = userId)
                val response = ApiClient.eventApi.deleteEvent(eventId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _eventDeletedStatus.value = true
                } else {
                    _eventDeletedStatus.value = false
                    _actionStatus.value = ActionStatus.Failure(response.body()?.error ?: "Silme başarısız oldu.")
                    Log.e("EventDetailViewModel", "Silme başarısız: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _eventDeletedStatus.value = false
                _actionStatus.value = ActionStatus.Failure("Silme sırasında bir hata oluştu.")
                Log.e("EventDetailViewModel", "Silme sırasında istisna oluştu", e)
            }
        }
    }
}