package com.example.loopin.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Sohbetler ve gruplar burada olacak"
    }
    val text: LiveData<String> = _text
}