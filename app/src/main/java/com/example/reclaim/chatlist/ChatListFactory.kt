package com.example.reclaim.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.home.HomeViewModel

class ChatListFactory(private val dao: ReclaimDatabaseDao): ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            return ChatListViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}