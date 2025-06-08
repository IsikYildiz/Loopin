package com.example.loopin.newchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemAddGroupMemberBinding // Yeni binding
import com.example.loopin.models.Friend
// import com.bumptech.glide.Glide // Eğer profil resimleri yüklüyorsanız

class AddGroupMembersAdapter(
    private val onCheckboxClick: (Friend, Boolean) -> Unit // Friend objesi ve isChecked durumu
) : ListAdapter<Friend, AddGroupMembersAdapter.AddGroupMemberViewHolder>(AddGroupMemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddGroupMemberViewHolder {
        val binding = ItemAddGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddGroupMemberViewHolder(binding, onCheckboxClick)
    }

    override fun onBindViewHolder(holder: AddGroupMemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AddGroupMemberViewHolder(
        private val binding: ItemAddGroupMemberBinding,
        private val onCheckboxClick: (Friend, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.apply {
                tvFriendName.text = friend.fullName
                // Profil resmi yükleme (Glide/Coil ile)
                // friend.profileImage?.let { imageUrl ->
                //     Glide.with(ivFriendProfile.context).load(imageUrl).into(ivFriendProfile)
                // } ?: ivFriendProfile.setImageResource(R.drawable.ic_profile_placeholder)

                // Checkbox durumu listener'ı
                cbSelectFriend.setOnCheckedChangeListener(null) // Önceki listener'ı temizle
                cbSelectFriend.isChecked = false // Varsayılan olarak işaretli değil (ViewModel'den durumu takip edin eğer persistence istiyorsanız)
                cbSelectFriend.setOnCheckedChangeListener { _, isChecked ->
                    onCheckboxClick(friend, isChecked)
                }

                // Tüm satıra tıklanınca da checkbox'ı toggle yapabiliriz
                root.setOnClickListener {
                    cbSelectFriend.isChecked = !cbSelectFriend.isChecked
                }
            }
        }
    }
}

class AddGroupMemberDiffCallback : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem == newItem
    }
}