package com.example.reclaim.data.source

import android.util.Log
import androidx.core.net.toUri
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.MessageType
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ChairRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "repository"
        const val COLLECTION_CHAT_ROOM = "chat_room"
        const val ROOM_KEY = "key"
        const val COLLECTION_CHAT_RECORD = "chat_record"
        const val UNREAD_TIMES = "unread_times"
        const val IS_SEEN = "is_seen"
        const val LAST_SENTENCE = "last_sentence"
        const val SEND_BY_ID = "send_by_id"
        const val SENT_TIME = "sent_time"
        const val SEND_VIDEO_CALL_TEXT = "我開啟了視訊，一起加入吧!"
        const val MEETING_ID = "meeting_id"
        const val MEETING_OVER = "meeting_over"
        const val CONTENT = "content"
        const val CHAT_ROOM_KEY = "chat_room_key"
        const val SENDER_NAME = "sender_name"
        const val ID = "ID"
        const val MESSAGE_TYPE = "message_type"
        const val USER_A_IMG = "user_a_img"
        const val USER_B_IMG = "user_b_img"
        const val USER_A_NAME = "user_a_name"
        const val USER_B_NAME = "user_b_name"
        const val MEETING_OVER_HINT = "通話已結束"


    }

    fun getAllRecordFromRoom(chatRoomKey: String, callback: (Query, DocumentSnapshot) -> Unit) {
        db.collection(COLLECTION_CHAT_ROOM).whereEqualTo(ROOM_KEY, chatRoomKey)
            .get().addOnSuccessListener { snapshots ->

                val room = snapshots.documents.first()
                val recordRegistration = room.reference.collection(COLLECTION_CHAT_RECORD).orderBy(
                    SENT_TIME, Query.Direction.ASCENDING
                )

                callback(recordRegistration, room)
            }
    }


    fun clearUnreadCounts(documentID: String) {

        val chatRoom = db.collection(COLLECTION_CHAT_ROOM)
            .document(documentID)
        chatRoom.get().addOnSuccessListener {

            chatRoom.update(UNREAD_TIMES, 0)
        }

    }

    fun updateSeenStatus(chatRoomID: String, documentID: String) {
        Log.i(TAG, "update seen status is trigger")
        val chatRoomIsSeenOrNot =
            db.collection(COLLECTION_CHAT_ROOM).document(chatRoomID).collection(
                COLLECTION_CHAT_RECORD
            )
                .document(documentID)
        chatRoomIsSeenOrNot.update(IS_SEEN, true)

    }

    fun sendMessage(
        content: String,
        type: MessageType,
        meetingId: String = "",
        chatRoom: ChatRoom,
        chatRoomDocumentID: String,
        senderName: String,
        senderImageUri: String
    ) {
        val currentTimeString = System.currentTimeMillis().toString()


        val data = hashMapOf(
            CHAT_ROOM_KEY to chatRoom.key,
            CONTENT to content,
            SENT_TIME to currentTimeString,
            SENDER_NAME to senderName,
            MESSAGE_TYPE to type,
            IS_SEEN to false,
            MEETING_ID to meetingId,
            USER_B_IMG to chatRoom.otherImage,
            USER_A_IMG to senderImageUri,
            USER_A_NAME to senderName,
            USER_B_NAME to chatRoom.otherName,
            MEETING_OVER to false
        )

        db.collection(COLLECTION_CHAT_ROOM).document(chatRoomDocumentID).collection(
            COLLECTION_CHAT_RECORD
        )
            .add(data).addOnSuccessListener {
                updateOnChatList(content, chatRoomDocumentID, currentTimeString)
                Log.i(TAG, "add data: ${it.id}")

            }.addOnFailureListener {
                Log.e(TAG, "error: $it")
            }
    }


    fun updateOnChatList(content: String, chatRoomKey: String, currentTimeString: String) {
        var currentUnreadTime = 0
        val chatRoom = db.collection(COLLECTION_CHAT_ROOM).document(chatRoomKey)
        chatRoom.get().addOnSuccessListener {
            currentUnreadTime = it.get(UNREAD_TIMES).toString().toInt()
            currentUnreadTime++

            val updateData = mapOf<String, String>(
                ROOM_KEY to chatRoomKey,
                LAST_SENTENCE to content,
                SEND_BY_ID to UserManager.userId,
                SENT_TIME to currentTimeString,
                UNREAD_TIMES to currentUnreadTime.toString()

            )
            chatRoom.update(updateData)
            Log.i(TAG, "successfullly")
        }.addOnFailureListener {
            Log.e(TAG, "update unread times failed: $it")
        }

    }


    fun sendVideoCallMessage(
        meetingId: String,
        chatRoom: ChatRoom,
        chatRoomDocumentID: String,
        senderName: String,
        senderImageUri: String
    ) {
        val text = SEND_VIDEO_CALL_TEXT
        val type = MessageType.VIDEO_CALL
        sendMessage(text, type, meetingId, chatRoom, chatRoomDocumentID, senderName, senderImageUri)
    }


    fun stopUserJoinMeeting(chatRoomDocumentID: String, meetingId: String) {

        val chatRoom = db.collection(COLLECTION_CHAT_ROOM).document(chatRoomDocumentID)
        var chatRecordDocumentID = ""
        chatRoom.collection(COLLECTION_CHAT_RECORD).whereEqualTo(MEETING_ID, meetingId).get()
            .addOnSuccessListener { snapshot ->

                chatRecordDocumentID = snapshot.documents.firstOrNull().let { it!!.id }

                val chatRecordDocument =
                    chatRoom.collection(COLLECTION_CHAT_RECORD).document(chatRecordDocumentID)

                val dataUpdateToChatRecord = hashMapOf<String, Any>(
                    MEETING_OVER to true,
                    CONTENT to MEETING_OVER_HINT
                )

                // update end of meeting text to chat record and chat list
                chatRecordDocument.update(dataUpdateToChatRecord)
                chatRoom.update(LAST_SENTENCE, MEETING_OVER_HINT)

            }
    }


    fun uploadImageToFireStorage(stringOfUri: String) {
        var currentUri = ""
        val reference = FirebaseStorage.getInstance().reference
        val path = UUID.randomUUID().leastSignificantBits
        val imageRef = reference.child("images/$path.jpg")

        val uploadTask = imageRef.putFile(stringOfUri.toUri())

        uploadTask.addOnSuccessListener { uri ->
            imageRef.downloadUrl.addOnSuccessListener {
                currentUri = it.toString()
                UserManager.userImage = it.toString()
                Log.i(TAG, "upload successfully, url is $it")
            }


        }
        Log.i(TAG, "currentUri is $currentUri")


    }


}