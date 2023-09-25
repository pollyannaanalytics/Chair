package com.example.reclaim.chatroom

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavArgs
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import io.grpc.Server

const val TAG = "ChatRoomViewModel"

class ChatRoomViewModel(reclaimDatabaseDao: ReclaimDatabaseDao, private val navArgs: ChatRoomFragmentArgs) :
    ViewModel() {
    companion object{
        private const val TAG = "ChatRoomViewModel"
    }

    private val chatRoomKey = navArgs.chatFriend.chatRoomKey

    private var _recordWithFriend = MutableLiveData<MutableList<ChatRecord>>()
    val recordWithFriend: LiveData<MutableList<ChatRecord>>
        get() = _recordWithFriend

    private var _noRecord = MutableLiveData<Boolean>()
    val noRecord: LiveData<Boolean>
        get() = _noRecord

    val db = Firebase.firestore

    val chatRoom = db.collection("chat_room").whereEqualTo("key", chatRoomKey)
        .orderBy("key", Query.Direction.DESCENDING)




    init {
        _recordWithFriend.value = emptyList<ChatRecord>().toMutableList()
        searchChatRoomWithFriend(chatRoom)

    }

    private fun getAllRecordFromRoom(room: DocumentSnapshot) {
        room.reference.collection("chat_record")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (document in snapshot) {
                        val chatRoomKey = document.get("chat_room_key").toString()
                        val content = document.get("content").toString()
                        val sendTime = document.get("sent_time").toString()
                        val sender = document.get("sender_id").toString()

                        val newRecord = ChatRecord(
                            chatRoomKey = chatRoomKey,
                            content = content,
                            sendTime = sendTime,
                            sender = sender
                        )

                        recordWithFriend.value?.add(newRecord)
                    }
                } else {

                }
            }
    }

    fun searchChatRoomWithFriend(chatRoom: Query) {


        chatRoom.addSnapshotListener { rooms, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (rooms != null) {
                val room = rooms.documents.get(0)
                getAllRecordFromRoom(room)
                Log.i(TAG, room.id.toString())

            } else {
                _noRecord.value = true
                Log.i(TAG, "this room is null")
            }
        }

    }


    fun sendMessage(text: String){
        val newRecord = ChatRecord(chatRoomKey = chatRoomKey, content = text, sendTime = "", sender = UserManager.userName)

        val data = hashMapOf(
            "chat_room_key" to newRecord.chatRoomKey,
            "content" to newRecord.content,
            "send_time" to newRecord.sendTime,
            "sender_name" to newRecord.sender
        )

        db.collection("chat_record").add(data).addOnSuccessListener {
            it.update("send_time", ServerValue.REQUEST_TIME_VALUE)
        }.addOnFailureListener {
            Log.i(TAG, "add record failed: $it")
        }
    }


}