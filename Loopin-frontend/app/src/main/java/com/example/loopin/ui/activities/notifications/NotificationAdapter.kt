package com.example.loopin.ui.activities.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.R
import com.example.loopin.models.Notification

class NotificationAdapter(
    private var notifications: MutableList<Notification>,
    private val onDeleteClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification, onDeleteClick)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.iconNotificationType)
        private val content: TextView = itemView.findViewById(R.id.textNotificationContent)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteNotification)

        fun bind(notification: Notification, onDeleteClick: (Notification) -> Unit) {
            // Bildirim tipine göre ikon ve metin ayarla
            when (notification.type) {
                "friend_request" -> {
                    icon.setImageResource(R.drawable.friend_request) // Örnek ikon
                    content.text = "You have a new friend request!"
                }
                "friend_request_accepted" -> {
                    icon.setImageResource(R.drawable.accept_icon) // Örnek ikon
                    content.text = "Your friend request was accepted."
                }
                "message" -> {
                    icon.setImageResource(R.drawable.chats_icon) // Örnek ikon
                    content.text = "You have a new message"
                }
                else -> {
                    icon.setImageResource(R.drawable.notification_icon) // Varsayılan
                    content.text = "You have a new notification"
                }
            }
            // Arka planı okundu/okunmadı durumuna göre ayarla (opsiyonel)
            if (notification.isRead == 1) {
                itemView.alpha = 0.6f
            } else {
                itemView.alpha = 1.0f
            }

            deleteButton.setOnClickListener {
                onDeleteClick(notification)
            }
        }
    }
}