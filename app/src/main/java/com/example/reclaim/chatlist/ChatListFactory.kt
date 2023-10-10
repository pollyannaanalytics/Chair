package com.example.reclaim.chatlist

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao


class ChatListFactory(private val dao: ReclaimDatabaseDao, private val activity: Activity): ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.M)
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            return ChatListViewModel(dao, activity) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}