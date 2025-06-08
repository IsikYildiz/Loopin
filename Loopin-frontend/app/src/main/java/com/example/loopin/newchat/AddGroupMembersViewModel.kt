package com.example.loopin.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.AddGroupMemberRequest
import com.example.loopin.models.Friend
import com.example.loopin.network.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGroupMembersViewModel @Inject constructor(
    private val apiClient: ApiClient
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _addGroupMemberStatus = MutableStateFlow<Pair<Boolean, String>?>(null) // (success, message)
    val addGroupMemberStatus: StateFlow<Pair<Boolean, String>?> = _addGroupMemberStatus.asStateFlow()

    private var allFriendsList: List<Friend> = emptyList() // Tüm arkadaşları tutmak için
    private val selectedFriends = mutableSetOf<Friend>() // Seçilen arkadaşları takip etmek için

    fun loadFriendsForGroup(groupId: Int) {
        viewModelScope.launch {
            try {
                val userId = PreferenceManager.getUserId()
                if (userId == null) {
                    println("Error: User ID not found.")
                    return@launch
                }

                val response = apiClient.friendApi.getUserFriends(userId)
                if (response.isSuccessful) {
                    val userFriends = response.body()?.friends ?: emptyList()

                    // Gruba zaten dahil olan üyeleri filtrele
                    val groupMembersResponse = apiClient.groupApi.getGroupMembers(groupId)
                    if (groupMembersResponse.isSuccessful) {
                        val groupMembers = groupMembersResponse.body()?.members?.map { it.userId }?.toSet() ?: emptySet()
                        allFriendsList = userFriends.filter { friend -> !groupMembers.contains(friend.userId) }
                    } else {
                        // Grup üyelerini yükleyemezsek, tüm arkadaşları göster (daha sonra filtreleme yapamayız)
                        allFriendsList = userFriends
                        println("Warning: Could not load group members to filter friends.")
                    }
                    _friends.value = allFriendsList
                } else {
                    println("Error loading friends for group: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading friends for group: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun searchFriends(query: String) {
        viewModelScope.launch {
            val filteredList = if (query.isBlank()) {
                allFriendsList
            } else {
                allFriendsList.filter {
                    it.fullName.contains(query, ignoreCase = true) ||
                            it.userName.contains(query, ignoreCase = true)
                }
            }
            _friends.value = filteredList
        }
    }

    fun selectFriend(friend: Friend) {
        selectedFriends.add(friend)
    }

    fun deselectFriend(friend: Friend) {
        selectedFriends.remove(friend)
    }

    fun addSelectedFriendsToGroup(groupId: Int) {
        if (selectedFriends.isEmpty()) {
            _addGroupMemberStatus.value = Pair(false, "Lütfen eklemek istediğiniz arkadaşları seçin.")
            return
        }

        viewModelScope.launch {
            val requesterId = PreferenceManager.getUserId()
            if (requesterId == null) {
                _addGroupMemberStatus.value = Pair(false, "Yetkiniz yok: Kullanıcı ID bulunamadı.")
                return@launch
            }

            var allSuccess = true
            val successMessages = mutableListOf<String>()
            val errorMessages = mutableListOf<String>()

            for (friend in selectedFriends) {
                try {
                    val request = AddGroupMemberRequest(userId = friend.userId, requesterId = requesterId)
                    val response = apiClient.groupApi.addGroupMember(groupId, request)
                    if (response.isSuccessful) {
                        successMessages.add("${friend.fullName} gruba eklendi.")
                    } else {
                        allSuccess = false
                        val errorMessage = response.errorBody()?.string() ?: "Bilinmeyen hata."
                        errorMessages.add("${friend.fullName} eklenemedi: $errorMessage")
                    }
                } catch (e: Exception) {
                    allSuccess = false
                    errorMessages.add("${friend.fullName} eklenirken hata: ${e.message}")
                    e.printStackTrace()
                }
            }

            // İşlemler bittikten sonra selectedFriends setini temizle
            selectedFriends.clear()

            if (allSuccess) {
                _addGroupMemberStatus.value = Pair(true, successMessages.joinToString("\n"))
            } else {
                _addGroupMemberStatus.value = Pair(false, "Bazı üyeler eklenemedi:\n" + errorMessages.joinToString("\n"))
            }
        }
    }

    fun resetAddGroupMemberStatus() {
        _addGroupMemberStatus.value = null
    }
}