package com.example.loopin.ui.activities.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.R
import com.example.loopin.models.FriendRequestInfo

class FriendRequestAdapter(
    private var requests: List<FriendRequestInfo>,
    private val onAcceptClick: (FriendRequestInfo) -> Unit,
    private val onRejectClick: (FriendRequestInfo) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request, onAcceptClick, onRejectClick)
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<FriendRequestInfo>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textRequestName)
        private val buttonAccept: Button = itemView.findViewById(R.id.buttonAccept)
        private val buttonReject: Button = itemView.findViewById(R.id.buttonReject)

        fun bind(
            request: FriendRequestInfo,
            onAccept: (FriendRequestInfo) -> Unit,
            onReject: (FriendRequestInfo) -> Unit
        ) {
            textName.text = "${request.fullName} size istek gönderdi."
            buttonAccept.setOnClickListener { onAccept(request) }
            buttonReject.setOnClickListener { onReject(request) }
        }
    }
}