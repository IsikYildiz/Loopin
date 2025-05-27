package com.example.loopin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Etkinlikler burada görüntülenecek"
    }
    val text: LiveData<String> = _text
}