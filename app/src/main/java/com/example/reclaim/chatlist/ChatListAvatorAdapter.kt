package com.example.reclaim.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.databinding.ChatlistItemBinding

class ChatListAvatorAdapter(private val onClickListener: OnClickListener):androidx.recyclerview.widget.ListAdapter<ChatRoom, ChatListAvatorAdapter.ImageViewHolder>(DiffCallback){

    class ImageViewHolder(private val binding: ChatlistItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(chatroom: ChatRoom){
            binding.chatRoom = chatroom
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ChatlistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(friend)
        }
    }

    class OnClickListener(val clickListener: (data: ChatRoom) -> Unit){
        fun onClick(data: ChatRoom) = clickListener(data)
    }


    companion object DiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.key == newItem.key
        }
    }
}