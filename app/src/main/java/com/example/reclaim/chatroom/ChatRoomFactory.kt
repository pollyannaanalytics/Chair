package com.example.reclaim.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao
import java.lang.IllegalArgumentException

class ChatRoomFactory(private val args: ChatRoomFragmentArgs, private val reclaimDatabaseDao: ReclaimDatabaseDao): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(reclaimDatabaseDao, args) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}