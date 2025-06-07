package com.example.loopin.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemEventBinding
import com.example.loopin.models.Event

// Etkinlik verilerini RecyclerView içinde göstermek için kullanılır.
// ListAdapter, liste güncellemelerini verimli bir şekilde yönetmemizi sağlar.
class EventAdapter(private val onItemClicked: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    // Bu fonksiyon, RecyclerView'ın yeni bir satıra ihtiyacı olduğunda çağrılır.
    // item_event.xml'i hafızaya yükler (inflate) ve bir ViewHolder içinde tutar.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    // Bu fonksiyon, RecyclerView bir satırı ekranda göstereceği zaman çağrılır.
    // O pozisyondaki veriyi (event) alıp ViewHolder aracılığıyla görünüme bağlar.
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        // Satırın tamamına tıklanma olayını ayarlar.
        holder.itemView.setOnClickListener {
            onItemClicked(event)
        }
    }

    // ViewHolder, her bir satırın içindeki görsel elemanları (TextView gibi) hafızada tutar.
    // Bu, her seferinde tekrar tekrar "findViewById" yapmamızı engeller.
    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.eventNameTextview.text = event.eventName
            binding.eventLocationTextview.text = event.eventLocation ?: "Konum belirtilmemiş"
            binding.eventTimeTextview.text = "${event.startTime} - ${event.endTime}"
        }
    }
}

// Bu yardımcı sınıf, listenin güncellenmesi gerektiğinde eski liste ile yeni liste
// arasındaki farkı hesaplar. Bu sayede sadece değişen satırlar güncellenir ve
// uygulama daha performanslı çalışır.
class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.eventId == newItem.eventId
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}