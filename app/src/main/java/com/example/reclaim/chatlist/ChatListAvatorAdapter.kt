package com.example.reclaim.chatlist

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.reclaim.data.Friends
import com.example.reclaim.databinding.ChatlistItemBinding
import com.example.reclaim.profile.ImageAdapter

class ChatListAvatorAdapter(private val onClickListener: OnClickListener):androidx.recyclerview.widget.ListAdapter<Friends, ChatListAvatorAdapter.ImageViewHolder>(DiffCallback){

    class ImageViewHolder(private val binding: ChatlistItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(friends: Friends){
            binding.friend = friends
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

    class OnClickListener(val clickListener: (data: Friends) -> Unit){
        fun onClick(data: Friends) = clickListener(data)
    }


    companion object DiffCallback : DiffUtil.ItemCallback<Friends>() {
        override fun areItemsTheSame(oldItem: Friends, newItem: Friends): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Friends, newItem: Friends): Boolean {
            return oldItem.userId == newItem.userId
        }
    }
}