package com.example.loopin.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loopin.databinding.ItemParticipantBinding
import com.example.loopin.models.Participant

class ParticipantAdapter(private val onItemClicked: (Participant) -> Unit) :
    ListAdapter<Participant, ParticipantAdapter.ParticipantViewHolder>(ParticipantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = getItem(position)
        holder.bind(participant)
        // Tıklama olayını burada set ediyoruz
        holder.itemView.setOnClickListener {
            onItemClicked(participant)
        }
    }

    inner class ParticipantViewHolder(private val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(participant: Participant) {
            binding.participantNameTextview.text = participant.name
        }
    }
}

class ParticipantDiffCallback : DiffUtil.ItemCallback<Participant>() {
    override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean {
        // Participant modelinde id alanı var
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean {
        return oldItem == newItem
    }
}