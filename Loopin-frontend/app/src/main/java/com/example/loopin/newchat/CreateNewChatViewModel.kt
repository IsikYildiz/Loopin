package com.example.loopin.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.CreateChatRequest
import com.example.loopin.models.Friend // Friend modelini kullanacağız
import com.example.loopin.network.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewChatViewModel @Inject constructor(
    private val apiClient: ApiClient
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList()) // Friend listesi tutacak
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _chatCreationStatus = MutableStateFlow<Pair<Int, String>?>(null) // Pair<chatId, chatTitle>
    val chatCreationStatus: StateFlow<Pair<Int, String>?> = _chatCreationStatus.asStateFlow()

    private var allUserFriends: List<Friend> = emptyList() // Tüm arkadaşları hafızada tutmak için

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            try {
                val userId = PreferenceManager.getUserId()
                if (userId == null) {
                    println("Error: User ID not found.")
                    return@launch
                }

                // Arkadaş listesini çekiyoruz
                val response = apiClient.friendApi.getUserFriends(userId)
                if (response.isSuccessful) {
                    allUserFriends = response.body()?.friends ?: emptyList()
                    _friends.value = allUserFriends
                } else {
                    println("Error loading friends: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading friends: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun searchFriends(query: String) {
        viewModelScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId == null) {
                println("Error: User ID not found for searching friends.")
                return@launch
            }

            if (query.isBlank()) {
                _friends.value = allUserFriends // Arama boşsa tüm arkadaşları göster
            } else {
                try {
                    // FriendApi'deki searchFriends metodunu kullanıyoruz
                    val response = apiClient.friendApi.searchFriends(userId, query)
                    if (response.isSuccessful) {
                        _friends.value = response.body()?.friends ?: emptyList()
                    } else {
                        println("Error searching friends: ${response.code()}")
                    }
                } catch (e: Exception) {
                    println("Exception searching friends: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun createChat(otherUserId: Int, otherUserName: String) {
        viewModelScope.launch {
            val currentUserId = PreferenceManager.getUserId()
            if (currentUserId == null) {
                println("Error: User ID not found for creating chat.")
                _chatCreationStatus.value = Pair(-1, "") // Hata durumu
                return@launch
            }

            try {
                val request = CreateChatRequest(user1Id = currentUserId, user2Id = otherUserId)
                val response = apiClient.chatApi.createChat(request)
                if (response.isSuccessful) {
                    val chatId = response.body()?.chatId
                    _chatCreationStatus.value = Pair(chatId ?: -1, otherUserName)
                } else {
                    println("Error creating chat: ${response.code()} - ${response.errorBody()?.string()}")
                    _chatCreationStatus.value = Pair(-1, "") // Hata durumu
                }
            } catch (e: Exception) {
                println("Exception creating chat: ${e.message}")
                e.printStackTrace()
                _chatCreationStatus.value = Pair(-1, "") // Hata durumu
            }
        }
    }

    fun resetChatCreationStatus() {
        _chatCreationStatus.value = null
    }
}