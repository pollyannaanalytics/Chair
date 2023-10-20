package com.example.reclaim.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import com.example.reclaim.chatroom.ChatRoomFragmentArgs
import com.example.reclaim.chatroom.ChatRoomViewModel
import com.example.reclaim.data.source.ChairRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val chairRepository: ChairRepository,
    private val chatRoomNavArgs: ChatRoomFragmentArgs
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(ChatRoomViewModel::class.java) ->
                    ChatRoomViewModel(chairRepository, chatRoomNavArgs)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}

