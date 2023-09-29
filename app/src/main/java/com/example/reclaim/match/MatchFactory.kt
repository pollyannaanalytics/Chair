package com.example.reclaim.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.chatroom.ChatRoomFragmentArgs
import com.example.reclaim.chatroom.ChatRoomViewModel
import com.example.reclaim.data.ReclaimDatabaseDao
import java.lang.IllegalArgumentException

class MatchFactory(private val args: MatchFragmentArgs, private val reclaimDatabaseDao: ReclaimDatabaseDao): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchViewModel::class.java)) {
            return MatchViewModel(args, reclaimDatabaseDao) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}