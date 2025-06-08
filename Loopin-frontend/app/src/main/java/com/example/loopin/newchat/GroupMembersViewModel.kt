package com.example.loopin.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.GroupMember
import com.example.loopin.models.RemoveGroupMemberRequest
import com.example.loopin.models.UpdateMemberRoleRequest
import com.example.loopin.network.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupMembersViewModel @Inject constructor(
    private val apiClient: ApiClient
) : ViewModel() {

    private val _groupMembers = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupMembers: StateFlow<List<GroupMember>> = _groupMembers.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String>("member")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _actionStatus = MutableStateFlow<Pair<Boolean, String>?>(null)
    val actionStatus: StateFlow<Pair<Boolean, String>?> = _actionStatus.asStateFlow()

    fun loadGroupMembers(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = apiClient.groupApi.getGroupMembers(groupId)
                if (response.isSuccessful) {
                    _groupMembers.value = response.body()?.members ?: emptyList()
                } else {
                    println("Error loading group members: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading group members: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadCurrentUserRole(groupId: Int, currentUserId: Int) {
        viewModelScope.launch {
            try {
                val response = apiClient.groupApi.getGroupMembers(groupId)
                if (response.isSuccessful) {
                    val currentUser = response.body()?.members?.find { it.userId == currentUserId }
                    _currentUserRole.value = currentUser?.role ?: "member"
                    // LOG EKLE: FAB görünürlüğünü kontrol etmek için
                    println("DEBUG: Current user role for FAB visibility: ${_currentUserRole.value}")
                } else {
                    println("Error loading current user role: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading current user role: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateMemberRole(groupId: Int, userIdToUpdate: Int, newRole: String) {
        viewModelScope.launch {
            val requesterId = PreferenceManager.getUserId()
            if (requesterId == null) {
                _actionStatus.value = Pair(false, "You don't have permission: User ID not found.") // English
                return@launch
            }
            try {
                val request = UpdateMemberRoleRequest(
                    userId = userIdToUpdate,
                    newRole = newRole,
                    requesterId = requesterId
                )
                val response = apiClient.groupApi.updateMemberRole(groupId, request)
                if (response.isSuccessful) {
                    _actionStatus.value = Pair(true, "Role of user ${userIdToUpdate} updated to $newRole.") // English
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to update role." // English
                    _actionStatus.value = Pair(false, errorMessage)
                }
            } catch (e: Exception) {
                _actionStatus.value = Pair(false, "Role update error: ${e.message}") // English
                e.printStackTrace()
            }
        }
    }

    fun removeGroupMember(groupId: Int, userIdToRemove: Int) {
        viewModelScope.launch {
            val requesterId = PreferenceManager.getUserId()
            if (requesterId == null) {
                _actionStatus.value = Pair(false, "You don't have permission: User ID not found.") // English
                return@launch
            }
            try {
                val request = RemoveGroupMemberRequest(
                    userId = userIdToRemove,
                    requesterId = requesterId
                )
                val response = apiClient.groupApi.removeGroupMember(groupId, request)
                if (response.isSuccessful) {
                    _actionStatus.value = Pair(true, "User ${userIdToRemove} removed from group.") // English
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to remove member." // English
                    _actionStatus.value = Pair(false, errorMessage)
                }
            } catch (e: Exception) {
                _actionStatus.value = Pair(false, "Member removal error: ${e.message}") // English
                e.printStackTrace()
            }
        }
    }

    fun resetActionStatus() {
        _actionStatus.value = null
    }
}