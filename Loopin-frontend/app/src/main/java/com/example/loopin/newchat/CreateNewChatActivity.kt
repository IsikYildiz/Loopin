package com.example.loopin.newchat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.PreferenceManager // PreferenceManager'ınızın yolu
import com.example.loopin.databinding.ActivityCreateNewChatBinding // Yeni binding
import com.example.loopin.models.Friend // Friend modelini kullanacağız
import com.example.loopin.ui.chats.ChatMessageActivity // ChatMessageActivity'nizin yolu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import com.example.loopin.newchat.UserSelectionAdapter

@AndroidEntryPoint
class CreateNewChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewChatBinding
    private val viewModel: CreateNewChatViewModel by viewModels()
    private lateinit var userAdapter: UserSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadFriends() // Başlangıçta arkadaşları yüklüyoruz
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Yeni Sohbet"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        // Friend modelini kullanacak şekilde UserSelectionAdapter'ı güncelledik
        userAdapter = UserSelectionAdapter { friend ->
            // Arkadaşa tıklandığında yeni birebir sohbet başlat
            viewModel.createChat(friend.userId, friend.fullName) // otherUserId ve otherUserName
        }
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(this@CreateNewChatActivity)
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        binding.btnCreateGroup.setOnClickListener {
            // Yeni grup oluşturma akışını başlat (örneğin ayrı bir aktiviteye yönlendir)
            val intent = Intent(this, CreateGroupActivity::class.java) // CreateGroupActivity'e yönlendir
            startActivity(intent)
        }

        binding.etSearch.addTextChangedListener { editable ->
            viewModel.searchFriends(editable.toString()) // Arkadaşları ara
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.friends.collectLatest { friends ->
                // Kendi kullanıcımızı listeden çıkarmaya gerek yok, friend listesi zaten bize gelen arkadaşları listeler
                userAdapter.submitList(friends)
                binding.tvFriendsCount.text = "${friends.size} Arkadaş" // Arkadaş sayısını güncelle
                if (friends.isEmpty() && binding.etSearch.text.isBlank()) {
                    binding.tvNoFriendsMessage.visibility = View.VISIBLE
                    binding.recyclerViewUsers.visibility = View.GONE
                } else {
                    binding.tvNoFriendsMessage.visibility = View.GONE
                    binding.recyclerViewUsers.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chatCreationStatus.collectLatest { chatInfo ->
                if (chatInfo != null) {
                    val (chatId, chatTitle) = chatInfo
                    if (chatId != -1) { // Sohbet başarıyla oluşturulduysa
                        Toast.makeText(this@CreateNewChatActivity, "Sohbet başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CreateNewChatActivity, ChatMessageActivity::class.java).apply {
                            putExtra("chatId", chatId)
                            putExtra("chatTitle", chatTitle) // Sohbet başlığını geçiriyoruz
                        }
                        startActivity(intent)
                        finish() // Bu aktiviteyi kapat
                        viewModel.resetChatCreationStatus() // Durumu sıfırla
                    } else { // Hata durumu
                        Toast.makeText(this@CreateNewChatActivity, "Sohbet oluşturulamadı. Zaten mevcut olabilir veya bir hata oluştu.", Toast.LENGTH_LONG).show()
                        viewModel.resetChatCreationStatus() // Durumu sıfırla
                    }
                }
            }
        }
    }
}