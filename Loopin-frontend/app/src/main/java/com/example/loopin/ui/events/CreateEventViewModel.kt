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

    // Sealed class, işlemin başarılı mı, başarısız mı olduğunu ve ek bilgi taşımasını sağlar.
    sealed class OperationResult {
        data class Success(val message: String, val eventId: Int?) : OperationResult()
        data class Failure(val errorMessage: String) : OperationResult()
    }

    private val _operationResult = MutableLiveData<OperationResult>()
    val operationResult: LiveData<OperationResult> = _operationResult
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
                // SADECE Etkinliği Oluşturma İsteği Gönderilecek
                val eventRequest = CreateEventRequest(
                    creatorId, eventName, eventLocation, startTime, endTime,
                    description, maxParticipants, isPrivate, password
                )
                val eventResponse = ApiClient.eventApi.createEvent(eventRequest)

                if (eventResponse.isSuccessful && eventResponse.body()?.success == true) {
                    // Başarılı! Backend artık grubu da oluşturuyor.
                    _operationResult.value = OperationResult.Success(
                        "Etkinlik ve sohbet grubu başarıyla oluşturuldu!",
                        eventResponse.body()?.eventId
                    )
                } else {
                    // Etkinlik oluşturma en başta başarısız oldu.
                    _operationResult.value = OperationResult.Failure(
                        "Etkinlik oluşturulamadı: ${eventResponse.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("CreateEventViewModel", "Etkinlik oluşturulurken hata", e)
                _operationResult.value = OperationResult.Failure("Bir ağ hatası oluştu.")
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
                // 1. API'ye güncelleme isteğini gönderiyoruz.
                val response = ApiClient.eventApi.updateEvent(eventId, request)

                // 2. Cevabı yeni OperationResult yapısına göre işliyoruz.
                if (response.isSuccessful && response.body()?.success == true) {
                    // Başarılı olursa: Success durumu ve güncellenen eventId ile LiveData'yı tetikle.
                    _operationResult.value = OperationResult.Success("Etkinlik başarıyla güncellendi!", eventId)
                } else {
                    // Başarısız olursa: Failure durumu ve sunucudan gelen hata mesajı ile LiveData'yı tetikle.
                    val errorMessage = response.body()?.error ?: "Etkinlik güncellenemedi."
                    Log.e("CreateEventViewModel", "Etkinlik güncellenemedi: ${response.errorBody()?.string()}")
                    _operationResult.value = OperationResult.Failure(errorMessage)
                }
            } catch (e: Exception) {
                // Bir istisna (örn: ağ hatası) olursa: Failure durumu ve genel hata mesajı ile LiveData'yı tetikle.
                Log.e("CreateEventViewModel", "Etkinlik güncellenirken hata", e)
                _operationResult.value = OperationResult.Failure("Güncelleme sırasında bir hata oluştu.")
            }
        }
    }

}