package com.example.reclaim.createprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.chatroom.ChatRoomViewModel
import com.example.reclaim.data.source.ChairRepository

class CreateProfileFactory(
    private val repository: ChairRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return CreateProfileViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}