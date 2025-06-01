package com.example.loopin.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Yaklaşan ve geçmiş etkinlikler burada görüntülenecek. Aynı zamanda buradan bir etkinlik oluşturulabilmeli"
    }
    val text: LiveData<String> = _text
}