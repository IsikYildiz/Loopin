package com.example.loopin.ui.chats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemChatBinding
import com.example.loopin.models.ChatInfo
import com.example.loopin.models.GroupInfo
import com.example.loopin.ui.chats.AllChatInfoItem // Import the new wrapper models
import com.example.loopin.ui.chats.AllGroupInfoItem
import com.example.loopin.ui.chats.ChatListItem

// Existing ChatAdapter (for Individual Chats)
class ChatAdapter(
    private val onItemClick: (ChatInfo) -> Unit
) : ListAdapter<ChatInfo, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(
        private val binding: ItemChatBinding,
        private val onItemClick: (ChatInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatInfo) {
            binding.apply {
                // You'll need the current user's ID here to correctly display the other user's name
                // For demonstration, let's assume current user ID is 1, but this should come from PreferenceManager
                val currentUserId = 1 // Placeholder, replace with actual user ID from PreferenceManager
                val otherUserName = if (chat.user1Id == currentUserId) chat.user2Name else chat.user1Name
                tvName.text = otherUserName
                tvLastMessage.text = chat.lastMessage ?: ""
                tvTime.text = formatTime(chat.lastMessageTime)

                root.setOnClickListener { onItemClick(chat) }
            }
        }

        private fun formatTime(time: String?): String {
            return time?.takeLast(5) ?: "" // "HH:mm" formatı için son 5 karakter
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatInfo>() {
    override fun areItemsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean {
        return oldItem.chatId == newItem.chatId
    }

    override fun areContentsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean {
        return oldItem == newItem
    }
}

// Existing GroupAdapter (for Group Chats)
class GroupAdapter(
    private val onItemClick: (GroupInfo) -> Unit
) : ListAdapter<GroupInfo, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GroupViewHolder(
        private val binding: ItemChatBinding,
        private val onItemClick: (GroupInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: GroupInfo) {
            binding.apply {
                tvName.text = group.groupName
                tvLastMessage.text = group.lastMessage ?: ""
                tvTime.text = formatTime(group.lastMessageTime)

                root.setOnClickListener { onItemClick(group) }
            }
        }

        private fun formatTime(time: String?): String {
            return time?.takeLast(5) ?: "" // "HH:mm" formatı için son 5 karakter
        }
    }
}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupInfo>() {
    override fun areItemsTheSame(oldItem: GroupInfo, newItem: GroupInfo): Boolean {
        return oldItem.groupId == newItem.groupId
    }

    override fun areContentsTheSame(oldItem: GroupInfo, newItem: GroupInfo): Boolean {
        return oldItem == newItem
    }
}

// NEW: Adapter for AllChatsFragment to handle both ChatInfo and GroupInfo using wrapper classes
class AllChatsAdapter(
    private val onItemClick: (ChatListItem) -> Unit,
    private val currentUserId: Int // Pass the current user ID for correct display of individual chat names
) : ListAdapter<ChatListItem, AllChatsAdapter.AllChatViewHolder>(AllChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllChatViewHolder(binding, onItemClick, currentUserId)
    }

    override fun onBindViewHolder(holder: AllChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AllChatViewHolder(
        private val binding: ItemChatBinding,
        private val onItemClick: (ChatListItem) -> Unit,
        private val currentUserId: Int
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatListItem: ChatListItem) {
            binding.apply {
                when (chatListItem) {
                    is AllChatInfoItem -> {
                        val chat = chatListItem.chat
                        val otherUserName = if (chat.user1Id == currentUserId) chat.user2Name else chat.user1Name
                        tvName.text = otherUserName
                        tvLastMessage.text = chat.lastMessage ?: ""
                        tvTime.text = formatTime(chat.lastMessageTime)
                    }
                    is AllGroupInfoItem -> {
                        val group = chatListItem.group
                        tvName.text = group.groupName
                        tvLastMessage.text = group.lastMessage ?: ""
                        tvTime.text = formatTime(group.lastMessageTime)
                    }
                }
                root.setOnClickListener { onItemClick(chatListItem) }
            }
        }

        private fun formatTime(time: String?): String {
            return time?.takeLast(5) ?: ""
        }
    }
}

class AllChatDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
    override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        // This is a deep comparison. If ChatInfo or GroupInfo have reference equality, this might not work as expected.
        // For data classes, it generally works based on content.
        return oldItem == newItem
    }
}