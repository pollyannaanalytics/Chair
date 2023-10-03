package com.example.reclaim.match

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
                if (it.isEmpty || it == null){
                    Log.e(TAG, "chat room is empty")

                }else{
                    documentId = it.documents.get(0).id

                    val data = hashMapOf(
                        "chat_room_key" to _chatRoomInfo.value!!.key,
                        "content" to text,
                        "sent_time" to System.currentTimeMillis(),
                        "sender_name" to UserManager.userName,
                        "id" to Random.nextLong(),
                        "message_type" to "message",
                        "meeting_id" to ""
                    )

                    FirebaseFirestore.getInstance().collection("chat_room").document(documentId)
                        .collection("chat_record").add(data).addOnSuccessListener {
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