package com.example.reclaim.chatroom

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.ChatRoomSendByMeBinding
import com.example.reclaim.databinding.ChatRoomSendByOtherBinding
import java.lang.ClassCastException

private const val ITEM_BY_ME = 0
private const val ITEM_BY_OTHER = 1

class ChatRoomAdapter(val context: Context, val list: List<ChatRecord>) :
    ListAdapter<ChatRecord, RecyclerView.ViewHolder>(DiffCallback) {
    class SendByMe(val binding: ChatRoomSendByMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ChatRecord){
            binding.chatRecord = record
            binding.executePendingBindings()
        }

    }

    class SendByOther(val binding: ChatRoomSendByOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ChatRecord) {
            binding.chatRecord = record
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_BY_ME -> SendByMe(
                ChatRoomSendByMeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ITEM_BY_OTHER -> SendByOther(
                ChatRoomSendByOtherBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

            else -> {
                throw ClassCastException("Unknown message $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    companion object DiffCallback : DiffUtil.ItemCallback<ChatRecord>() {
        override fun areItemsTheSame(oldItem: ChatRecord, newItem: ChatRecord): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatRecord, newItem: ChatRecord): Boolean {
            return oldItem.id == newItem.id
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == UserManager.userName) {
            ITEM_BY_ME

        } else {
            ITEM_BY_OTHER
        }
    }
}