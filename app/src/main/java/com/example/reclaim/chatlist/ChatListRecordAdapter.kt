package com.example.reclaim.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.databinding.ChatListRecordItemBinding

class ChatListRecordAdapter(private val onClickListener: OnClickListener): ListAdapter<ChatRoom, ChatListRecordAdapter.RecordViewHolder>(ChatListAvatorAdapter.DiffCallback){
    class RecordViewHolder(private val binding: ChatListRecordItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(chatRoom: ChatRoom){
            binding.chatRoomRecord = chatRoom
        }
    }

    class OnClickListener(val clickListener: (data: ChatRoom) -> Unit){
        fun onClick(data: ChatRoom) = clickListener(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        return RecordViewHolder(
            ChatListRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)

        holder.bind(record)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(record)
        }
    }


}

