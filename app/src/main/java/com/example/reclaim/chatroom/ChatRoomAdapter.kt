package com.example.reclaim.chatroom

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ScrollView
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


class ChatRoomAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<ChatRecord, RecyclerView.ViewHolder>(DiffCallback) {
    private val TAG = "ChatRoomAdapter"
    class SendByMe(val binding: ChatRoomSendByMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ChatRecord){
            binding.chatRecord = record
            binding.executePendingBindings()
        }

    }

    class SendByOther(val binding: ChatRoomSendByOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ChatRecord, onClickListener: OnClickListener) {
            binding.chatRecord = record
            binding.executePendingBindings()

            if (record.type == "videocall"){
                if (!record.meetingOver){
                    binding.videoInvitationBtn.visibility = View.VISIBLE
                    binding.videoInvitationBtn.setOnClickListener {
                        onClickListener.onClick(record)


                        Log.i(TAG, "record is : $record")
                    }
                }else{
                    binding.videoInvitationBtn.visibility = View.GONE
                    binding.sendByOtherText.text = "通話已結束"
                }

            }else{
                binding.videoInvitationBtn.visibility = View.GONE
            }
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


        val recordItem = getItem(position)

        if (recordItem.sender == UserManager.userName){
            (holder as SendByMe).bind(recordItem)

        }else{
            (holder as SendByOther).bind(recordItem, onClickListener)
        }
//        when(holder){
//            is SendByOther -> holder.bind(recordItem)
//            is SendByMe -> holder.bind(recordItem)
//        }
    }

    class OnClickListener(val clickListener: (record: ChatRecord) -> Unit) {
        fun onClick(record: ChatRecord) = clickListener(record)
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