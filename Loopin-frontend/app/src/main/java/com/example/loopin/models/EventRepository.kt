package com.example.loopin.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.loopin.models.Event
import java.util.Date
import kotlin.collections.find
import kotlin.collections.maxOfOrNull
import kotlin.collections.toMutableList

object EventRepository {

    // Artık başlangıçta boş bir liste var.
    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    fun addEvent(event: Event) {
        val currentList = _events.value?.toMutableList() ?: mutableListOf()
        currentList.add(event)
        _events.value = currentList
    }

    // Bu fonksiyon hala CreateEventViewModel tarafından kullanıldığı için kalabilir.
    fun getNextEventId(): Int {
        val currentList = _events.value ?: emptyList()
        return (currentList.maxOfOrNull { it.eventId } ?: 0) + 1
    }

    // Bu fonksiyon EventActivity tarafından kullanılıyor, şimdilik kalabilir.
    fun getEventById(id: Int): Event? {
        val currentList = _events.value ?: emptyList()
        return currentList.find { it.eventId == id }
    }
}