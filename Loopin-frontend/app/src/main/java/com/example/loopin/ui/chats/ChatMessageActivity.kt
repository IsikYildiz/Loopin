package com.example.loopin.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityChatMessageBinding
import com.example.loopin.databinding.ItemMessageBinding
// Make sure these imports are correct if you moved Message and GroupMessage
import com.example.loopin.models.BaseMessage // Import BaseMessage
import com.example.loopin.models.SendGroupMessageRequest
import com.example.loopin.models.SendMessageRequest
import com.example.loopin.network.ApiClient
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.View
import com.example.loopin.databinding.ItemMessageReceivedBinding
import com.example.loopin.databinding.ItemMessageSentBinding
import javax.inject.Inject

@AndroidEntryPoint
class ChatMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMessageBinding
    private val viewModel: ChatMessageViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()

        val chatId = intent.getIntExtra("chatId", -1)
        val groupId = intent.getIntExtra("groupId", -1)

        if (chatId != -1) {
            viewModel.loadChatMessages(chatId)
        } else if (groupId != -1) {
            viewModel.loadGroupMessages(groupId)
        }
        val chatTitle = intent.getStringExtra("chatTitle") ?: "Chat"
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatMessageActivity)
            adapter = this@ChatMessageActivity.adapter
        }

        lifecycleScope.launchWhenStarted {
            viewModel.messages.collectLatest { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }
}

@HiltViewModel
class ChatMessageViewModel @Inject constructor(
    private val apiClient: ApiClient
) : ViewModel() {

    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    private var currentChatId: Int = -1
    private var currentGroupId: Int = -1

    fun loadChatMessages(chatId: Int) {
        currentChatId = chatId
        viewModelScope.launch {
            try {
                val response = apiClient.chatApi.getChatMessages(chatId)
                if (response.isSuccessful) {
                    _messages.value = response.body()?.messages ?: emptyList()
                } else {
                    println("Error loading chat messages: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading chat messages: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadGroupMessages(groupId: Int) {
        currentGroupId = groupId
        viewModelScope.launch {
            try {
                val response = apiClient.groupApi.getGroupMessages(groupId)
                if (response.isSuccessful) {
                    _messages.value = response.body()?.messages ?: emptyList()
                } else {
                    println("Error loading group messages: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading group messages: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val userId = PreferenceManager.getUserId()
                if (userId == null) {
                    println("Error: User ID not found for sending message.")
                    return@launch
                }

                if (currentChatId != -1) {
                    val request = SendMessageRequest(
                        senderId = userId,
                        content = content
                    )
                    val response = apiClient.chatApi.sendMessage(currentChatId, request)
                    if (response.isSuccessful) {
                        loadChatMessages(currentChatId) // Refresh messages after sending
                    } else {
                        println("Error sending chat message: ${response.code()}")
                    }
                } else if (currentGroupId != -1) {
                    val request = SendGroupMessageRequest(
                        senderId = userId,
                        content = content
                    )
                    val response = apiClient.groupApi.sendGroupMessage(currentGroupId, request)
                    if (response.isSuccessful) {
                        loadGroupMessages(currentGroupId) // Refresh messages after sending
                    } else {
                        println("Error sending group message: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                println("Exception sending message: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

// ChatMessageActivity.kt içinde MessageAdapter sınıfı
class MessageAdapter : ListAdapter<BaseMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // Mevcut kullanıcının ID'sini PreferenceManager'dan alacak şekilde constructor'ı güncelleyelim.
    private var currentUserId: Int = PreferenceManager.getUserId() ?: -1

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        // Mesajı gönderen kişi mevcut kullanıcı ise VIEW_TYPE_SENT, değilse VIEW_TYPE_RECEIVED döndür.
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MessageViewHolder(binding.root) // Burada CardView'in root'u yerine direkt binding.root kullanıldı
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MessageViewHolder(binding.root) // Burada CardView'in root'u yerine direkt binding.root kullanıldı
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message, getItemViewType(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: BaseMessage, viewType: Int) {
            if (viewType == VIEW_TYPE_SENT) {
                val binding = ItemMessageSentBinding.bind(itemView)
                binding.textMessageContent.text = message.content
                binding.textMessageTime.text = message.sentAt.takeLast(5) // Zamanı göster
            } else { // VIEW_TYPE_RECEIVED
                val binding = ItemMessageReceivedBinding.bind(itemView)
                binding.textMessageSender.text = message.senderName
                binding.textMessageContent.text = message.content
                binding.textMessageTime.text = message.sentAt.takeLast(5) // Zamanı göster
            }
        }
    }
}

// MessageDiffCallback can now safely use oldItem == newItem because BaseMessage
// guarantees that data classes will override equals and hashCode correctly.
class MessageDiffCallback : DiffUtil.ItemCallback<BaseMessage>() {
    override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        // This will now correctly use the data class's generated equals method,
        // and the lint warning about Object's equals will be gone.
        return oldItem == newItem
    }
}