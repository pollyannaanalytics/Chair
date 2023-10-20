package com.example.reclaim.data.source

import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.MessageType
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class ChairRepository(private val remoteDataSource: ChairRemoteDataSource) {



    fun updateSeenStatus(chatRoomID: String, documentID: String){
        remoteDataSource.updateSeenStatus(chatRoomID, documentID)
    }

    fun sendMessage(content: String, type: MessageType, meetingID: String = "", chatRoom: ChatRoom, chatRoomDocumentID: String){
        remoteDataSource.sendMessage(content, type, meetingID, chatRoom, chatRoomDocumentID)
    }

    fun updateOnChatList(content: String, chatRoomKey: String, currentTimeString: String){
        remoteDataSource.updateOnChatList(content, chatRoomKey, currentTimeString)
    }

    fun sendVideoCallMessage(meetingID: String, chatRoom: ChatRoom, chatRoomDocumentID: String){
        remoteDataSource.sendVideoCallMessage(meetingID, chatRoom, chatRoomDocumentID)
    }

    fun stopUserJoinMeeting(chatRoomDocumentID: String, meetingID: String){
        remoteDataSource.stopUserJoinMeeting(chatRoomDocumentID, meetingID)
    }

    fun clearUnreadTimes(documentID: String){
        remoteDataSource.clearUnreadTimes(documentID)
    }

    fun getAllRecordFromRoom(chatRoomKey: String, callback: (Query, DocumentSnapshot) -> Unit){
        remoteDataSource.getAllRecordFromRoom(chatRoomKey, callback)
    }
}