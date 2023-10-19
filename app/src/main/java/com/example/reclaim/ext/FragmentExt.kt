package com.example.reclaim.ext

import androidx.fragment.app.Fragment
import com.example.reclaim.ChairApplication
import com.example.reclaim.chatroom.ChatRoomFactory
import com.example.reclaim.chatroom.ChatRoomFragmentArgs

fun Fragment.getVmFactory(chatRoom: ChatRoomFragmentArgs): ChatRoomFactory {
    val repository = (requireContext().applicationContext as ChairApplication).chairRepository
    return ChatRoomFactory(repository, chatRoom)
}