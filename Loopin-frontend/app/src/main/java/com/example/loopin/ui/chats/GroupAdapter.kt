package com.example.loopin.ui.chats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemGroupChatBinding
import com.example.loopin.models.GroupInfo

class GroupAdapter(private val onGroupClicked: (GroupInfo) -> Unit) :
    ListAdapter<GroupInfo, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onGroupClicked(group)
        }
    }

    class GroupViewHolder(private val binding: ItemGroupChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: GroupInfo) {
            binding.textViewGroupName.text = group.groupName
            binding.textViewLastMessage.text = group.lastMessage ?: "Henüz mesaj yok."
            // Glide veya Picasso gibi bir kütüphane ile grup resmini de yükleyebilirsiniz.
            // binding.imageViewGroupImage.load(group.groupImage)
        }
    }

    private class GroupDiffCallback : DiffUtil.ItemCallback<GroupInfo>() {
        override fun areItemsTheSame(oldItem: GroupInfo, newItem: GroupInfo): Boolean {
            return oldItem.groupId == newItem.groupId
        }

        override fun areContentsTheSame(oldItem: GroupInfo, newItem: GroupInfo): Boolean {
            return oldItem == newItem
        }
    }
}