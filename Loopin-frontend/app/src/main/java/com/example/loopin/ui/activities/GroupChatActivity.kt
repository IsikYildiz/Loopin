package com.example.loopin.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.databinding.ActivityGroupChatBinding
import com.example.loopin.ui.chats.GroupChatViewModel
import com.example.loopin.ui.chats.MessageAdapter

// Intent'ten veri alırken kullanacağımız anahtarlar (keys)
const val EXTRA_GROUP_ID = "GROUP_ID"
const val EXTRA_GROUP_NAME = "GROUP_NAME"

class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var viewModel: GroupChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    private var groupId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bir önceki ekrandan gönderilen grup ID ve adını alıyoruz.
        groupId = intent.getIntExtra(EXTRA_GROUP_ID, -1)
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME)

        // Eğer grup ID'si gelmemişse bir hata vardır, ekranı kapat.
        if (groupId == -1) {
            Toast.makeText(this, "Grup bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Toolbar'ı ayarla ve grup adını başlık yap.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = groupName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ViewModel'i başlat
        viewModel = ViewModelProvider(this).get(GroupChatViewModel::class.java)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Ekran açıldığında mesajları yükle
        viewModel.fetchMessages(groupId)
    }

    // Geri tuşuna basıldığında aktiviteyi bitirir.
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.recyclerViewMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@GroupChatActivity).apply {
                // Yeni mesajlar geldiğinde listenin en altına odaklanmak için
                stackFromEnd = true
            }
        }
    }

    private fun setupListeners() {
        binding.buttonSend.setOnClickListener {
            val messageContent = binding.editTextMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                viewModel.sendMessage(groupId, messageContent)
            }
        }
    }

    private fun observeViewModel() {
        // Mesaj listesini gözlemle
        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitList(messages) {
                // Veri güncellendikten sonra listenin sonuna git
                binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
            }
        }

        // Yükleme durumunu gözlemle
        viewModel.isLoading.observe(this) { isLoading ->
            // (İsteğe bağlı) Yükleniyor indicator'ı gösterebilirsiniz.
        }

        // Hata durumunu gözlemle
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Mesaj gönderme durumunu gözlemle
        viewModel.messageSentStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                // Mesaj başarıyla gönderilince yazı yazdığımız alanı temizle
                binding.editTextMessage.text.clear()
            } else {
                Toast.makeText(this, "Mesaj gönderilemedi.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}