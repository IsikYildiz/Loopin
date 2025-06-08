package com.example.loopin.ui.activities.notifications

import com.example.loopin.databinding.ActivityNotificationsBinding
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityNotificationBinding
import com.example.loopin.ui.activities.notifications.NotificationAdapter
import com.example.loopin.network.ApiClient
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Bildirimler"

        PreferenceManager.init(this)
        currentUserId = PreferenceManager.getUserId() ?: -1

        if (currentUserId == -1) {
            Toast.makeText(this, "Kullanıcı bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(mutableListOf()) { notification ->
            deleteNotification(notification.notificationId)
        }
        binding.recyclerNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
        }
    }

    private fun loadNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiClient.notificationApi.getUserNotifications(currentUserId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notifications = response.body()!!.notifications
                    if (notifications.isEmpty()) {
                        binding.textNoNotifications.visibility = View.VISIBLE
                    } else {
                        binding.textNoNotifications.visibility = View.GONE
                        notificationAdapter.updateData(notifications)
                    }
                } else {
                    Toast.makeText(this@NotificationsActivity, "Couldn't load notifications.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("NotificationsActivity", "Error loading notifications", e)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun deleteNotification(notificationId: Int) {
        lifecycleScope.launch {
            try {
                val userId = PreferenceManager.getUserId()
                val response =
                    userId?.let { ApiClient.notificationApi.deleteNotification(it, notificationId) }
                if (response != null) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@NotificationsActivity, "Deleted notification.", Toast.LENGTH_SHORT).show()
                        loadNotifications() // Listeyi yenile
                    } else {
                        Toast.makeText(this@NotificationsActivity, "Couldn't delete notification.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("NotificationsActivity", "Error deleting notification", e)
            }
        }
    }
}