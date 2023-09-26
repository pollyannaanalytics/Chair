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
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import io.grpc.Server
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

const val TAG = "ChatRoomViewModel"

class ChatRoomViewModel(
    reclaimDatabaseDao: ReclaimDatabaseDao,
    private val navArgs: ChatRoomFragmentArgs
) :
    ViewModel() {
    companion object {
        private const val TAG = "ChatRoomViewModel"
    }

    private val chatRoomKey = navArgs.chatFriend.chatRoomKey

    private var _recordWithFriend = MutableLiveData<MutableList<ChatRecord>>()
    val recordWithFriend: LiveData<MutableList<ChatRecord>>
        get() = _recordWithFriend

    private var _noRecord = MutableLiveData<Boolean>()
    val noRecord: LiveData<Boolean>
        get() = _noRecord

    private val db = Firebase.firestore

    private val chatRoom = db.collection("chat_room").whereEqualTo("key", chatRoomKey)
        .orderBy("send", Query.Direction.DESCENDING)

    private var _onDestroyed = MutableLiveData<Boolean>()
    val onDestroyed: LiveData<Boolean>
        get() = _onDestroyed


    init {
        _onDestroyed.value = false
        _recordWithFriend.value = emptyList<ChatRecord>().toMutableList()
        searchChatRoomWithFriend(chatRoom)

    }

    private fun getAllRecordFromRoom(room: DocumentSnapshot) {
        _recordWithFriend.value!!.clear()
        val regitration = room.reference.collection("chat_record")
            .orderBy("send_time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, error.toString())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentRecord = emptyList<ChatRecord>().toMutableList()

                    for (document in snapshot) {

                        val id = document.get("id").toString()
                        val chatRoomKey = document.get("chat_room_key").toString()
                        val content = document.get("content").toString()
                        val timeStamp = document.get("sent_time").toString()
                        val sender = document.get("sender_name").toString()
                        val isProcessed = document.get("isProcessed").toString()

                        val sendTime = changeTimeStampToHHMMFormat(timeStamp)

                        val newRecord = ChatRecord(
                            id = id.toLong(),
                            chatRoomKey = chatRoomKey,
                            content = content,
                            sendTime = sendTime,
                            sender = sender
                        )
                        if (isProcessed == "false") {
                            currentRecord.add(newRecord)

                        }



                        Log.i(TAG, "get record: ${room.id}")
                    }

                    _recordWithFriend.value = currentRecord
                } else {
                    Log.i(TAG, "record is null")
                }
            }
        regitration

        if (_onDestroyed.value == true) {
            regitration.remove()
        }
    }

    private fun changeTimeStampToHHMMFormat(timeStamp: String): String {
        val timeStampToLong = timeStamp.toLong()
        val minuteInt = ((timeStampToLong / (1000 * 60)) % 60)
        val hourInt = ((timeStampToLong / (1000 * 60 * 60)) % 24)
        var minutString = ""
        var hourString = ""

        minutString = if (minuteInt < 10) {
            "0$minuteInt"
        } else {
            minuteInt.toString()
        }
        hourString = if(hourInt < 10){
            "0$hourInt"
        }else{
            hourInt.toString()
        }

        val timeFormatResult = "$hourString : $minutString"

        return timeFormatResult
    }

    fun searchChatRoomWithFriend(chatRoom: Query) {


        chatRoom.addSnapshotListener { rooms, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (rooms != null && !rooms.isEmpty) {
                val room = rooms.documents.get(0)
                getAllRecordFromRoom(room)
                Log.i(TAG, room.id.toString())

            } else {
                _noRecord.value = true
                Log.i(TAG, "this room is null")
            }
        }

    }


    private fun updateSentTime(chatRoomKey: String, currentTime: String, documentId: String) {
        val updateDocument =
            db.collection("chat_room").document(chatRoomKey).collection("chat_record")
                .document(documentId)
        updateDocument.update("send_time", currentTime)
//        updateDocument.update("isProcessed", true)
        Log.i(TAG, "successfully update sendtime")
    }

    fun sendMessage(text: String) {

        val newRecord = ChatRecord(
            id = Random.nextLong(),
            chatRoomKey = chatRoomKey,
            content = text,
            sendTime = "",
            sender = UserManager.userName
        )

        val data = hashMapOf(
            "chat_room_key" to newRecord.chatRoomKey,
            "content" to newRecord.content,
            "send_time" to newRecord.sendTime,
            "sender_name" to newRecord.sender,
            "id" to newRecord.id,
            "isProcessed" to false
        )

        db.collection("chat_room").document(newRecord.chatRoomKey).collection("chat_record")
            .add(data).addOnSuccessListener {
            val currentTime = ServerValue.REQUEST_TIME_VALUE.toString()
            updateSentTime(newRecord.chatRoomKey, currentTime, it.id)
        }

        updateOnFriendList(text, chatRoomKey)


    }

    private fun updateOnFriendList(text: String, chatRoomKey: String) {
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room").document(chatRoomKey)
        chatRoom.update("last_sentence", text)
        chatRoom.update("send_by_id", UserManager.userId)

    }


}