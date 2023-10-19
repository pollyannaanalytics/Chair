package com.example.reclaim.data.source

import com.google.firebase.firestore.CollectionReference

interface ChairRepository {

    fun getAllRecordFromRoom(chatRoomKey: String): CollectionReference

    fun updateSeenStatus(chatRoomID: String, documentID: String)

    fun sendMessage(sendText: String, type: String, meetingID: String = "")

    fun updateOnChatList(sendText: String, chatRoomKey: String, currentTime: String)

    fun sendVideoCallMessage(meetingID: String)

    fun turnOffJoinBtn()
}