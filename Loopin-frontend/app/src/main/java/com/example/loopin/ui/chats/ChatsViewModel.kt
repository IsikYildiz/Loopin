package com.example.loopin.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.ChatInfo
import com.example.loopin.models.GroupInfo
import com.example.loopin.network.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Interface for common properties needed for sorting and identification in AllChatsAdapter
sealed interface ChatListItem {
    val id: Int
    val lastMessageTime: String?
}

// Wrapper class for ChatInfo to be used in AllChatsAdapter
data class AllChatInfoItem(val chat: ChatInfo) : ChatListItem {
    override val id: Int = chat.chatId
    override val lastMessageTime: String? = chat.lastMessageTime
}

// Wrapper class for GroupInfo to be used in AllChatsAdapter
data class AllGroupInfoItem(val group: GroupInfo) : ChatListItem {
    override val id: Int = group.groupId
    override val lastMessageTime: String? = group.lastMessageTime
}

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val apiClient: ApiClient
) : ViewModel() {

    // _allChats now holds a list of the wrapper interface type
    private val _allChats = MutableStateFlow<List<ChatListItem>>(emptyList())
    val allChats: StateFlow<List<ChatListItem>> = _allChats.asStateFlow()

    private val _individualChats = MutableStateFlow<List<ChatInfo>>(emptyList())
    val individualChats: StateFlow<List<ChatInfo>> = _individualChats.asStateFlow()

    private val _groupChats = MutableStateFlow<List<GroupInfo>>(emptyList())
    val groupChats: StateFlow<List<GroupInfo>> = _groupChats.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                val userId = PreferenceManager.getUserId() // Assuming PreferenceManager exists and provides userId
                if (userId == null) {
                    println("Error: User ID not found.")
                    return@launch
                }

                // Load individual chats
                val chatsResponse = apiClient.chatApi.getUserChats(userId)
                if (chatsResponse.isSuccessful) {
                    _individualChats.value = chatsResponse.body()?.chats ?: emptyList()
                } else {
                    println("Error loading individual chats: ${chatsResponse.code()}")
                }

                // Load group chats
                val groupsResponse = apiClient.groupApi.getUserGroups(userId)
                if (groupsResponse.isSuccessful) {
                    _groupChats.value = groupsResponse.body()?.groups ?: emptyList()
                } else {
                    println("Error loading group chats: ${groupsResponse.code()}")
                }

                // Combine and sort all chats using the wrapper classes
                val combinedList = mutableListOf<ChatListItem>()
                _individualChats.value.forEach { chatInfo ->
                    combinedList.add(AllChatInfoItem(chatInfo))
                }
                _groupChats.value.forEach { groupInfo ->
                    combinedList.add(AllGroupInfoItem(groupInfo))
                }

                _allChats.value = combinedList.sortedByDescending {
                    it.lastMessageTime // Directly access lastMessageTime from ChatListItem
                }

            } catch (e: Exception) {
                println("Exception loading chats: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refreshChats() {
        loadChats()
    }
}