package com.example.reclaim.data.source

import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.MessageType
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class ChairRepository(private val remoteDataSource: ChairRemoteDataSource) {



    fun updateSeenStatus(chatRoomID: String, documentID: String){
        remoteDataSource.updateSeenStatus(chatRoomID, documentID)
    }

    fun sendMessage(content: String, type: MessageType, meetingID: String = "", chatRoom: ChatRoom, chatRoomDocumentID: String, senderName: String, senderImageUri:String){
        remoteDataSource.sendMessage(content, type, meetingID, chatRoom, chatRoomDocumentID, senderName, senderImageUri)
    }

    fun sendVideoCallMessage(meetingID: String, chatRoom: ChatRoom, chatRoomDocumentID: String, senderName: String, senderImageUri: String){
        remoteDataSource.sendVideoCallMessage(meetingID, chatRoom, chatRoomDocumentID, senderName, senderImageUri)
    }

    fun stopUserJoinMeeting(chatRoomDocumentID: String, meetingID: String){
        remoteDataSource.stopUserJoinMeeting(chatRoomDocumentID, meetingID)
    }

    fun clearUnreadCounts(documentID: String){
        remoteDataSource.clearUnreadCounts(documentID)
    }

    fun getAllRecordFromRoom(chatRoomKey: String, callback: (Query, DocumentSnapshot) -> Unit){
        remoteDataSource.getAllRecordFromRoom(chatRoomKey, callback)
    }

    fun uploadImageToFireStorage(stringOfUri: String){
        remoteDataSource.uploadImageToFireStorage(stringOfUri)
    }

    fun uploadUserProfile(callback: (DocumentReference) -> Unit){
        remoteDataSource.uploadUserProfile(callback)
    }
}