package com.example.loopin.ui.chats

import com.example.loopin.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import android.widget.Toast
import com.example.loopin.databinding.ItemMessageReceivedBinding
import com.example.loopin.databinding.ItemMessageSentBinding
import com.example.loopin.models.DeleteChatRequest
import com.example.loopin.models.DeleteGroupRequest
import com.example.loopin.newchat.GroupMembersActivity
import javax.inject.Inject

@AndroidEntryPoint
class ChatMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMessageBinding
    private val viewModel: ChatMessageViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    private var currentChatId: Int = -1
    private var currentGroupId: Int = -1
    private var isGroupChat: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar) // Toolbar'ı ayarla
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        currentChatId = intent.getIntExtra("chatId", -1)
        currentGroupId = intent.getIntExtra("groupId", -1)
        val chatTitle = intent.getStringExtra("chatTitle") ?: "Sohbet"

        supportActionBar?.title = chatTitle // Toolbar başlığını ayarla

        if (currentChatId != -1) {
            isGroupChat = false
            viewModel.loadChatMessages(currentChatId)
        } else if (currentGroupId != -1) {
            isGroupChat = true
            viewModel.loadGroupMessages(currentGroupId)
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_message_menu, menu)
        val deleteChatItem = menu?.findItem(R.id.action_delete_chat)
        val groupMembersItem = menu?.findItem(R.id.action_group_members)
        val deleteGroupItem = menu?.findItem(R.id.action_delete_group) // Yeni menü öğesi

        if (isGroupChat) {
            deleteChatItem?.isVisible = false // Birebir sohbeti sil seçeneği görünmez
            groupMembersItem?.isVisible = true // Grup üyeleri seçeneği görünür
            deleteGroupItem?.isVisible = false // Başlangıçta gizli, rol kontrolü ile açılacak

            // Grup oluşturucusu ise grup silme seçeneğini göster
            lifecycleScope.launch {
                viewModel.isGroupCreator.collectLatest { isCreator ->
                    deleteGroupItem?.isVisible = isCreator
                }
            }
            // Grup bilgilerini yükleyelim
            if (currentGroupId != -1) {
                viewModel.loadGroupInfo(currentGroupId)
            }

        } else {
            deleteChatItem?.isVisible = true // Birebir sohbeti sil seçeneği görünür
            groupMembersItem?.isVisible = false // Grup üyeleri seçeneği görünmez
            deleteGroupItem?.isVisible = false // Grup silme seçeneği görünmez
        }
        return true
    }

    // Menü öğesi seçildiğinde
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_chat -> {
                if (!isGroupChat && currentChatId != -1) {
                    viewModel.deleteChat(currentChatId)
                }
                true
            }
            R.id.action_group_members -> {
                if (isGroupChat && currentGroupId != -1) {
                    val intent = Intent(this, GroupMembersActivity::class.java).apply {
                        putExtra("groupId", currentGroupId)
                    }
                    startActivity(intent)
                }
                true
            }
            R.id.action_delete_group -> { // Yeni grup silme menü öğesi
                if (isGroupChat && currentGroupId != -1) {
                    viewModel.deleteGroup(currentGroupId)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatMessageActivity)
            adapter = this@ChatMessageActivity.adapter
        }

        lifecycleScope.launch {
            viewModel.groupDeletionStatus.collectLatest { isSuccess ->
                if (isSuccess == true) {
                    Toast.makeText(this@ChatMessageActivity, "Group Deleted Succesfully", Toast.LENGTH_SHORT).show()
                    finish() // Aktiviteyi kapat
                    viewModel.resetGroupDeletionStatus()
                } else if (isSuccess == false) {
                    Toast.makeText(this@ChatMessageActivity, "Group couldn't deleted", Toast.LENGTH_SHORT).show()
                    viewModel.resetGroupDeletionStatus()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.messages.collectLatest { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chatDeletionStatus.collectLatest { isSuccess ->
                if (isSuccess == true) {
                    Toast.makeText(this@ChatMessageActivity, "Sohbet başarıyla silindi.", Toast.LENGTH_SHORT).show()
                    finish() // Aktiviteyi kapat
                    viewModel.resetChatDeletionStatus()
                } else if (isSuccess == false) {
                    Toast.makeText(this@ChatMessageActivity, "Sohbet silinemedi.", Toast.LENGTH_SHORT).show()
                    viewModel.resetChatDeletionStatus()
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

    private val _groupDeletionStatus = MutableStateFlow<Boolean?>(null)
    val groupDeletionStatus: StateFlow<Boolean?> = _groupDeletionStatus.asStateFlow()

    private val _isGroupCreator = MutableStateFlow<Boolean>(false)
    val isGroupCreator: StateFlow<Boolean> = _isGroupCreator.asStateFlow()

    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    private val _chatDeletionStatus = MutableStateFlow<Boolean?>(null) // null: bekliyor, true: başarılı, false: başarısız
    val chatDeletionStatus: StateFlow<Boolean?> = _chatDeletionStatus.asStateFlow()

    private var currentChatId: Int = -1
    private var currentGroupId: Int = -1

    fun loadGroupInfo(groupId: Int) {
        viewModelScope.launch {
            try {
                val userId = PreferenceManager.getUserId()
                if (userId == null) return@launch

                val response = apiClient.groupApi.getGroupById(groupId)
                if (response.isSuccessful) {
                    val group = response.body()?.group
                    if (group != null) {
                        _isGroupCreator.value = (group.createdBy == userId)
                    }
                } else {
                    println("Error loading group info: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Exception loading group info: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId == null) {
                println("Error: User ID not found for deleting group.")
                _groupDeletionStatus.value = false
                return@launch
            }
            try {
                val request = DeleteGroupRequest(userId = userId) // DeleteGroupRequest modelini kullanıyoruz
                val response = apiClient.groupApi.deleteGroup(groupId, request)
                if (response.isSuccessful) {
                    _groupDeletionStatus.value = true
                } else {
                    println("Error deleting group: ${response.code()}")
                    _groupDeletionStatus.value = false
                }
            } catch (e: Exception) {
                println("Exception deleting group: ${e.message}")
                e.printStackTrace()
                _groupDeletionStatus.value = false
            }
        }
    }

    fun resetGroupDeletionStatus() {
        _groupDeletionStatus.value = null
    }

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
                        loadChatMessages(currentChatId)
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
                        loadGroupMessages(currentGroupId)
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

    fun deleteChat(chatId: Int) {
        viewModelScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId == null) {
                println("Error: User ID not found for deleting chat.")
                _chatDeletionStatus.value = false
                return@launch
            }
            try {
                val request = DeleteChatRequest(userId = userId) // DeleteChatRequest modelini kullanıyoruz
                val response = apiClient.chatApi.deleteChat(chatId, request)
                if (response.isSuccessful) {
                    _chatDeletionStatus.value = true
                } else {
                    println("Error deleting chat: ${response.code()}")
                    _chatDeletionStatus.value = false
                }
            } catch (e: Exception) {
                println("Exception deleting chat: ${e.message}")
                e.printStackTrace()
                _chatDeletionStatus.value = false
            }
        }
    }

    fun resetChatDeletionStatus() {
        _chatDeletionStatus.value = null
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