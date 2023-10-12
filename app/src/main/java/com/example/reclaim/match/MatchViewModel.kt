package com.example.reclaim.match

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlin.random.Random

private const val TAG = "MatchViewModel"

class MatchViewModel(
    private val navArgs: MatchFragmentArgs,
    private val reclaimDatabaseDao: ReclaimDatabaseDao
) : ViewModel() {

    private var _chatRoomInfo = MutableLiveData<ChatRoom>()
    val chatRoomInfo: LiveData<ChatRoom>
        get() = _chatRoomInfo

    private var _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _chatRoomInfo.value = navArgs.chatRoom
        Log.i(com.example.reclaim.match.TAG, "chat room info: ${_chatRoomInfo.value}")
    }

    fun sendMessageToChatRoom(text: String) {
        var documentId = ""
        FirebaseFirestore.getInstance().collection("chat_room").whereEqualTo(
            "key", _chatRoomInfo.value?.let { it.key }
        )
            .get()
            .addOnSuccessListener {
                if (it.isEmpty || it == null) {
                    Log.e(TAG, "chat room is empty")

                } else {
                    documentId = it.documents.get(0).id

                    val newRecord = ChatRecord(
                        id = Random.nextLong(),
                        chatRoomKey = _chatRoomInfo.value.let { it!!.key },
                        content = text,
                        sendTime = System.currentTimeMillis().toString(),
                        sender = UserManager.userName,
                        type = "message",
                        meetingId = "",
                        otherImage = _chatRoomInfo.value.let { it!!.otherImage },
                        selfImage = UserManager.userImage,
                        selfName = UserManager.userName,
                        otherName = _chatRoomInfo.value.let { it!!.otherName },
                        isSeen = false,
                        meetingOver = false
                    )


                    val data = hashMapOf(
                        "chat_room_key" to newRecord.chatRoomKey,
                        "content" to newRecord.content,
                        "sent_time" to newRecord.sendTime,
                        "sender_name" to newRecord.sender,
                        "id" to newRecord.id,
                        "message_type" to newRecord.type,
                        "is_seen" to newRecord.isSeen,
                        "meeting_id" to newRecord.meetingId,
                        "user_b_img" to newRecord.otherImage,
                        "user_a_img" to UserManager.userImage,
                        "user_a_name" to newRecord.selfName,
                        "user_b_name" to newRecord.otherName,
                        "meeting_over" to newRecord.meetingOver
                    )


                    FirebaseFirestore.getInstance().collection("chat_room").document(documentId)
                        .collection("chat_record").add(data).addOnSuccessListener {
                            val chatRoom = FirebaseFirestore.getInstance().collection("chat_room")
                                .document(documentId)
                            chatRoom.update("last_sentence", text)
                            chatRoom.update("send_by_id", UserManager.userId)
                            chatRoom.update("sent_time", System.currentTimeMillis())
                            chatRoom.update("unread_times", + 1)
                            Log.i(TAG, "add chat message successfully")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "cannot add record: $it")
                        }

                }


            }.addOnFailureListener {
                Log.e(TAG, "send message failed: $it")
            }


    }

}