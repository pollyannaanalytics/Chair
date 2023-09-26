package com.example.reclaim.chatroom

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    val friend = navArgs.chatFriend

    private var _recordWithFriend = MutableLiveData<MutableList<ChatRecord>>()
    val recordWithFriend: LiveData<MutableList<ChatRecord>>
        get() = _recordWithFriend

    private var _noRecord = MutableLiveData<Boolean>()
    val noRecord: LiveData<Boolean>
        get() = _noRecord

    private val db = Firebase.firestore

    private val chatRoom = db.collection("chat_room").whereEqualTo("key", chatRoomKey)


    private var _onDestroyed = MutableLiveData<Boolean>()
    val onDestroyed: LiveData<Boolean>
        get() = _onDestroyed

    private var _joinMeetingId = MutableLiveData<String>()
    val joinMeetingId: LiveData<String>
        get() = _joinMeetingId



    init {
        _onDestroyed.value = false
        _recordWithFriend.value = emptyList<ChatRecord>().toMutableList()
        searchChatRoomWithFriend(chatRoom)

    }

    private fun getAllRecordFromRoom(room: DocumentSnapshot) {
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
                        val type = document.get("message_type").toString()
                        val meetingId = document.get("meeting_id").toString()




                        val newRecord = ChatRecord(
                            id = id.toLong(),
                            chatRoomKey = chatRoomKey,
                            content = content,
                            sendTime = timeStamp,
                            sender = sender,
                            type = type,
                            meetingId = meetingId
                        )


                        currentRecord.add(newRecord)
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

        val registration = chatRoom.addSnapshotListener { rooms, error ->
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

        registration

        if (_onDestroyed.value == true){
            registration.remove()
        }

    }


    private fun updateSentTime(chatRoomKey: String, currentTime: String, documentId: String){
        val updateDocument =
            db.collection("chat_room").document(chatRoomKey).collection("chat_record")
                .document(documentId)
        updateDocument.update("send_time", currentTime)
//        updateDocument.update("isProcessed", true)
        Log.i(TAG, "successfully update sendtime")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, type: String, meetingId: String = "") {
        val sendTimeInTaiwan = setUpTime()

        val newRecord = ChatRecord(
            id = Random.nextLong(),
            chatRoomKey = chatRoomKey,
            content = text,
            sendTime = sendTimeInTaiwan,
            sender = UserManager.userName,
            type = type,
            meetingId = meetingId
        )

        val data = hashMapOf(
            "chat_room_key" to newRecord.chatRoomKey,
            "content" to newRecord.content,
            "sent_time" to newRecord.sendTime,
            "sender_name" to newRecord.sender,
            "id" to newRecord.id,
            "message_type" to type,
            "meeting_id" to meetingId
        )

        db.collection("chat_room").document(newRecord.chatRoomKey).collection("chat_record")
            .add(data).addOnSuccessListener {
            val currentTime = System.currentTimeMillis().toString()
            updateSentTime(newRecord.chatRoomKey, currentTime, it.id)
                updateOnFriendList(text, chatRoomKey, currentTime)
        }




    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpTime(): String {
        val taipeiClock = Clock.system(ZoneId.of("Asia/Taipei"))
        val localTimeNow = LocalTime.now(taipeiClock)

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return localTimeNow.format(formatter)

    }

    private fun updateOnFriendList(text: String, chatRoomKey: String, currentTime: String) {
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room").document(chatRoomKey)
        chatRoom.update("last_sentence", text)
        chatRoom.update("send_by_id", UserManager.userId)
        chatRoom.update("sent_time",currentTime )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVideoCallMessage(meetingId: String){
        val text = "我開啟了視訊，一起加入吧! Meeting ID: $meetingId"
        val type = "videocall"
        sendMessage(text, type, meetingId)

    }

    fun joinMeeting(meetingId: String) {
        _joinMeetingId.value = meetingId
    }

    fun removeListener(){
        _onDestroyed.value = true
    }

}