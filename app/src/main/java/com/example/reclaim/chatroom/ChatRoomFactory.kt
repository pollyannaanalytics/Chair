package com.example.reclaim.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.source.ChairRepository
import java.lang.IllegalArgumentException

class ChatRoomFactory(
    private val chairRepository: ChairRepository,
    private val args: ChatRoomFragmentArgs
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(chairRepository, args) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}