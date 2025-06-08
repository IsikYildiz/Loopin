package com.example.loopin.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopin.PreferenceManager
import com.example.loopin.models.GroupInfo
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class ChatsViewModel : ViewModel() {

    private val _groups = MutableLiveData<List<GroupInfo>>()
    val groups: LiveData<List<GroupInfo>> = _groups

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchUserGroups() {
        val userId = PreferenceManager.getUserId()
        if (userId == null || userId == -1) {
            _error.value = "Lütfen giriş yapın."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.groupApi.getUserGroups(userId = userId)
                if (response.isSuccessful && response.body() != null) {
                    _groups.value = response.body()!!.groups
                } else {
                    _error.value = "Gruplar yüklenemedi."
                    Log.e("ChatsViewModel", "Hata: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.value = "Ağ hatası oluştu."
                Log.e("ChatsViewModel", "İstisna:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}