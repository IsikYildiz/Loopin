package com.example.loopin.ui.activities.friends

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityFriendsBinding
import com.example.loopin.models.FriendRequest
import com.example.loopin.models.FriendRequestInfo
import com.example.loopin.network.ApiClient
import com.example.loopin.ui.activities.friends.FriendListAdapter
import com.example.loopin.ui.activities.friends.FriendRequestAdapter
import kotlinx.coroutines.launch

class FriendsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var friendAdapter: FriendListAdapter
    private lateinit var requestAdapter: FriendRequestAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Arkadaşlar"

        PreferenceManager.init(applicationContext)
        currentUserId = PreferenceManager.getUserId() ?: -1

        if (currentUserId == -1) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerViews()
        loadData()
    }

    private fun setupRecyclerViews() {
        // Friends Adapter
        friendAdapter = FriendListAdapter(emptyList()) { friend ->
            removeFriend(friend.userId)
        }
        binding.recyclerFriends.layoutManager = LinearLayoutManager(this)
        binding.recyclerFriends.adapter = friendAdapter

        // Requests Adapter
        requestAdapter = FriendRequestAdapter(emptyList(),
            onAcceptClick = { request -> acceptRequest(request) },
            onRejectClick = { request -> rejectRequest(request) }
        )
        binding.recyclerFriendRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerFriendRequests.adapter = requestAdapter
    }

    private fun loadData() {
        loadFriends()
        loadFriendRequests()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.friendApi.getUserFriends(currentUserId)
                if (response.isSuccessful && response.body()?.success == true) {
                    friendAdapter.updateData(response.body()!!.friends)
                } else {
                    Toast.makeText(this@FriendsActivity, "Failed to load friends.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFriendRequests() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.friendApi.getPendingFriendRequests(currentUserId, type = "incoming")
                if (response.isSuccessful && response.body()?.success == true) {
                    requestAdapter.updateData(response.body()!!.requests)
                } else {
                    Toast.makeText(this@FriendsActivity, "Failed to load requests.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeFriend(friendId: Int) {
        lifecycleScope.launch {
            val userId = PreferenceManager.getUserId()
            try {
                val response = userId?.let { ApiClient.friendApi.removeFriend(it,friendId) }
                if (response != null) {
                    if (response.isSuccessful && response!!.body()?.success == true) {
                        Toast.makeText(this@FriendsActivity, "Friend removed.", Toast.LENGTH_SHORT).show()
                        loadData() // Listeyi yenile
                    } else {
                        Toast.makeText(this@FriendsActivity, "Failed to remove friend.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptRequest(requestInfo: FriendRequestInfo) {
        lifecycleScope.launch {
            val request = FriendRequest(senderId = requestInfo.senderId, receiverId = currentUserId)
            try {
                val response = ApiClient.friendApi.acceptFriendRequest(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@FriendsActivity, "Request accepted.", Toast.LENGTH_SHORT).show()
                    loadData() // Listeleri yenile
                } else {
                    Toast.makeText(this@FriendsActivity, "Failed to accept.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectRequest(requestInfo: FriendRequestInfo) {
        lifecycleScope.launch {
            val request = FriendRequest(senderId = requestInfo.senderId, receiverId = currentUserId)
            try {
                val response = ApiClient.friendApi.rejectFriendRequest(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@FriendsActivity, "Request rejected.", Toast.LENGTH_SHORT).show()
                    loadData() // Listeleri yenile
                } else {
                    Toast.makeText(this@FriendsActivity, "Failed to reject.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}