package com.example.loopin.ui.chats // Paket adınızı kontrol edin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ItemMessageReceivedBinding
import com.example.loopin.databinding.ItemMessageSentBinding
import com.example.loopin.models.GroupMessage

private const val VIEW_TYPE_SENT = 1
private const val VIEW_TYPE_RECEIVED = 2

class MessageAdapter : ListAdapter<GroupMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val currentUserId = PreferenceManager.getUserId()

    /**
     * Bu fonksiyon, listenin her bir elemanının tipini belirler.
     * Mesajı gönderenin ID'si bizim ID'mizle eşleşiyorsa "SENT", eşleşmiyorsa "RECEIVED" tipini döndürür.
     */
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    /**
     * Belirlenen view tipine göre doğru layout'u ve ViewHolder'ı oluşturur.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else { // VIEW_TYPE_RECEIVED
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    /**
     * Doğru ViewHolder'a veriyi bağlar.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder.itemViewType == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    // Gönderilen mesajlar için ViewHolder
    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.textMessageContent.text = message.content
        }
    }

    // Alınan mesajlar için ViewHolder
    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.textMessageContent.text = message.content
            binding.textMessageSender.text = message.senderName
        }
    }
}

// ListAdapter'ın verimliliği için gerekli DiffUtil.ItemCallback sınıfı
class MessageDiffCallback : DiffUtil.ItemCallback<GroupMessage>() {
    override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
        return oldItem == newItem
    }
}