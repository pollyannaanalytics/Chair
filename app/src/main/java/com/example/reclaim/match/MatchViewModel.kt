package com.example.reclaim.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ReclaimDatabaseDao

class MatchViewModel(private val navArgs: MatchFragmentArgs, private val reclaimDatabaseDao: ReclaimDatabaseDao): ViewModel() {

    private var _chatRoomInfo = MutableLiveData<ChatRoom>()
    val chatRoomInfo: LiveData<ChatRoom>
        get() = _chatRoomInfo

    init {
        _chatRoomInfo.value = navArgs.chatRoom
    }

}