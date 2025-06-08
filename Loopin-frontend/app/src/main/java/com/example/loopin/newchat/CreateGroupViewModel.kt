package com.example.loopin.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager // PreferenceManager'ınızın yolu
import com.example.loopin.models.CreateGroupRequest // Model import'ı
import com.example.loopin.network.ApiClient // API istemcisi import'ı
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val apiClient: ApiClient // ApiClient inject ediliyor
) : ViewModel() {

    private val _groupCreationStatus = MutableStateFlow<Int?>(null) // null: bekliyor, -1: hata, >0: groupId
    val groupCreationStatus: StateFlow<Int?> = _groupCreationStatus.asStateFlow()

    fun createGroup(groupName: String, groupDescription: String?, groupImage: String? = null) {
        viewModelScope.launch {
            val createdByUserId = PreferenceManager.getUserId()
            if (createdByUserId == null) {
                println("Error: User ID not found for creating group.")
                _groupCreationStatus.value = -1 // Hata durumu
                return@launch
            }

            try {
                val request = CreateGroupRequest(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    createdBy = createdByUserId,
                    groupImage = groupImage
                )
                val response = apiClient.groupApi.createGroup(request) // API çağrısı
                if (response.isSuccessful) {
                    val groupId = response.body()?.groupId
                    _groupCreationStatus.value = groupId ?: -1 // Başarılıysa groupId, değilse -1
                } else {
                    println("Error creating group: ${response.code()} - ${response.errorBody()?.string()}")
                    _groupCreationStatus.value = -1 // Hata durumu
                }
            } catch (e: Exception) {
                println("Exception creating group: ${e.message}")
                e.printStackTrace()
                _groupCreationStatus.value = -1 // Hata durumu
            }
        }
    }

    fun resetGroupCreationStatus() {
        _groupCreationStatus.value = null
    }
}