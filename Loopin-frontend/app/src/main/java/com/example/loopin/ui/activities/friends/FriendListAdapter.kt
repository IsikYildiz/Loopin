package com.example.loopin.ui.activities.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.R
import com.example.loopin.models.Friend

class FriendListAdapter(
    private var friends: List<Friend>,
    private val onRemoveClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend, onRemoveClick)
    }

    override fun getItemCount(): Int = friends.size

    fun updateData(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textFriendName)
        private val buttonRemove: Button = itemView.findViewById(R.id.buttonRemoveFriend)

        fun bind(friend: Friend, onRemoveClick: (Friend) -> Unit) {
            textName.text = friend.fullName
            buttonRemove.setOnClickListener { onRemoveClick(friend) }
        }
    }
}