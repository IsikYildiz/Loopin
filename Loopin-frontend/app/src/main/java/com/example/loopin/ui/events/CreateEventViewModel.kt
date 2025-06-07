package com.example.loopin.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.models.CreateEventRequest
import com.example.loopin.models.Event
import com.example.loopin.models.UpdateEventRequest
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class CreateEventViewModel : ViewModel() {

    private val _operationStatus = MutableLiveData<Boolean>()
    val operationStatus: LiveData<Boolean> = _operationStatus

    private val _eventToEdit = MutableLiveData<Event?>()
    val eventToEdit: LiveData<Event?> = _eventToEdit

    fun loadEvent(eventId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.eventApi.getEventById(eventId)
                if (response.isSuccessful) {
                    _eventToEdit.value = response.body()?.event
                } else {
                    _eventToEdit.value = null
                    Log.e("CreateEventViewModel", "Etkinlik yüklenemedi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _eventToEdit.value = null
                Log.e("CreateEventViewModel", "Etkinlik yüklenirken hata", e)
            }
        }
    }

    fun createEvent(
        creatorId: Int,
        eventName: String,
        eventLocation: String?,
        startTime: String,
        endTime: String,
        description: String?,
        maxParticipants: Int,
        isPrivate: Int,
        password: String?
    ) {
        viewModelScope.launch {
            try {
                val request = CreateEventRequest(
                    creatorId = creatorId,
                    eventName = eventName,
                    eventLocation = eventLocation,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    maxParticipants = maxParticipants,
                    isPrivate = isPrivate,
                    password = password
                )
                val response = ApiClient.eventApi.createEvent(request)
                _operationStatus.value = response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                Log.e("CreateEventViewModel", "Etkinlik oluşturulurken hata", e)
                _operationStatus.value = false
            }
        }
    }

    fun updateEvent(
        eventId: Int,
        eventName: String,
        eventLocation: String?,
        startTime: String,
        endTime: String,
        description: String?,
        maxParticipants: Int,
        isPrivate: Int,
        password: String?
    ) {
        viewModelScope.launch {
            try {
                val request = UpdateEventRequest(
                    eventName = eventName,
                    eventLocation = eventLocation,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    maxParticipants = maxParticipants,
                    isPrivate = (isPrivate == 1),
                    password = password
                )
                // Gerçek API isteğini burada yapıyoruz.
                val response = ApiClient.eventApi.updateEvent(eventId, request)
                // Başarı durumunu API'den gelen cevaba göre ayarlıyoruz.
                _operationStatus.value = response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                Log.e("CreateEventViewModel", "Etkinlik güncellenirken hata", e)
                _operationStatus.value = false
            }
        }
    }
}