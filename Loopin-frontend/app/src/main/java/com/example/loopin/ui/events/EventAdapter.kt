package com.example.loopin.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemEventBinding
import com.example.loopin.models.Event
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventAdapter(private val onItemClicked: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        holder.itemView.setOnClickListener {
            onItemClicked(event)
        }
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // API'den gelen tarih formatını (ISO 8601 veya benzeri) parse etmek için
        private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        private val apiDateFormatAlternative = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Ekranda gösterilecek tarih ve saat formatları
        private val displayDateFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR"))
        private val displayTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        private fun parseDate(dateString: String?): java.util.Date? {
            if (dateString.isNullOrEmpty()) return null
            return try {
                if (dateString.contains("T")) {
                    apiDateFormat.parse(dateString)
                } else {
                    apiDateFormatAlternative.parse(dateString)
                }
            } catch (e: Exception) {
                null
            }
        }

        fun bind(event: Event) {
            val startDate = parseDate(event.startTime)
            val endDate = parseDate(event.endTime)

            binding.eventNameTextview.text = event.eventName
            binding.eventLocationTextview.text = event.eventLocation ?: "Konum belirtilmemiş"
            binding.eventTimeTextview.text = "${event.startTime} - ${event.endTime}"

            binding.eventDateTextview.text = if (startDate != null) {
                displayDateFormat.format(startDate)
            } else {
                "Tarih belirtilmemiş"
            }

            binding.eventTimeTextview.text = if (startDate != null && endDate != null) {
                "${displayTimeFormat.format(startDate)} - ${displayTimeFormat.format(endDate)}"
            } else if (startDate != null) {
                displayTimeFormat.format(startDate)
            } else {
                "Saat belirtilmemiş"
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.eventId == newItem.eventId
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}