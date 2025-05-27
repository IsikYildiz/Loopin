package com.example.loopin.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Takvim özelliğine burada erişilecek."
    }
    val text: LiveData<String> = _text
}