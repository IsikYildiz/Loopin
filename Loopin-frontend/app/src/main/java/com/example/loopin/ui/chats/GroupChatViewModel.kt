package com.example.loopin.ui.chats // Paket adınızı kontrol edin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.GroupMessage
import com.example.loopin.models.SendGroupMessageRequest
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class GroupChatViewModel : ViewModel() {

    // Mesaj listesini tutacak LiveData
    private val _messages = MutableLiveData<List<GroupMessage>>()
    val messages: LiveData<List<GroupMessage>> = _messages

    // Yükleme durumunu kontrol etmek için LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Hata mesajlarını göstermek için LiveData
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Mesaj gönderme işleminin sonucunu bildirmek için LiveData
    private val _messageSentStatus = MutableLiveData<Boolean>()
    val messageSentStatus: LiveData<Boolean> = _messageSentStatus


    /**
     * Belirtilen grup ID'sine ait mesajları API'den çeker.
     */
    fun fetchMessages(groupId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.groupApi.getGroupMessages(groupId = groupId, limit = 100) // Son 100 mesajı çekelim
                if (response.isSuccessful && response.body() != null) {
                    _messages.value = response.body()!!.messages
                } else {
                    _error.value = "Mesajlar yüklenemedi."
                    Log.e("GroupChatViewModel", "Mesajlar çekilemedi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.value = "Bir ağ hatası oluştu."
                Log.e("GroupChatViewModel", "Mesajlar çekilirken istisna oluştu", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Belirtilen gruba yeni bir mesaj gönderir.
     */
    fun sendMessage(groupId: Int, content: String) {
        val senderId = PreferenceManager.getUserId()
        if (senderId == null || senderId == -1) {
            _error.value = "Mesaj gönderilemedi: Kullanıcı ID bulunamadı."
            return
        }

        if (content.isBlank()) {
            _error.value = "Boş mesaj gönderilemez."
            return
        }

        viewModelScope.launch {
            try {
                val request = SendGroupMessageRequest(senderId = senderId, content = content)
                val response = ApiClient.groupApi.sendGroupMessage(groupId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _messageSentStatus.value = true // Başarı durumunu bildir
                    fetchMessages(groupId) // Mesaj gönderdikten sonra listeyi anında güncelle
                } else {
                    _messageSentStatus.value = false // Hata durumunu bildir
                    Log.e("GroupChatViewModel", "Mesaj gönderilemedi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _messageSentStatus.value = false
                Log.e("GroupChatViewModel", "Mesaj gönderilirken istisna oluştu", e)
            }
        }
    }
}