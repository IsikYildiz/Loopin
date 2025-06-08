package com.example.loopin.newchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemGroupMemberBinding // Yeni binding
import com.example.loopin.models.GroupMember // GroupMember modelini import edin

class GroupMembersAdapter(
    private val onItemClick: (GroupMember) -> Unit
) : ListAdapter<GroupMember, GroupMembersAdapter.GroupMemberViewHolder>(GroupMemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        val binding = ItemGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupMemberViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GroupMemberViewHolder(
        private val binding: ItemGroupMemberBinding,
        private val onItemClick: (GroupMember) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: GroupMember) {
            binding.apply {
                tvMemberName.text = member.fullName
                tvMemberRole.text = member.role

                root.setOnClickListener { onItemClick(member) }
            }
        }
    }
}

class GroupMemberDiffCallback : DiffUtil.ItemCallback<GroupMember>() {
    override fun areItemsTheSame(oldItem: GroupMember, newItem: GroupMember): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: GroupMember, newItem: GroupMember): Boolean {
        return oldItem == newItem
    }
}