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
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone
import kotlin.random.Random

const val TAG = "ChatRoomViewModel"

class ChatRoomViewModel(
    private val reclaimDatabaseDao: ReclaimDatabaseDao,
    private val navArgs: ChatRoomFragmentArgs
) :
    ViewModel() {
    companion object {
        private const val TAG = "ChatRoomViewModel"
    }

    private val chatRoomKey = navArgs.chatRoom.key
    val friend = navArgs.chatRoom.key
    val friendImage = navArgs.chatRoom.otherImage
    val friendName = navArgs.chatRoom.otherName

    private var _recordWithFriend = MutableLiveData<MutableList<ChatRecord>>()
    val recordWithFriend: LiveData<MutableList<ChatRecord>>
        get() = _recordWithFriend

    private var _noRecord = MutableLiveData<Boolean>()
    val noRecord: LiveData<Boolean>
        get() = _noRecord

    private val db = Firebase.firestore

    var _documentID = ""
    var _meetingId = ""


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
        Log.i(TAG, "start to get data")
        val regitration = room.reference.collection("chat_record")
            .orderBy("sent_time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "this is error: ${error.message}")


                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentRecord = mutableListOf<ChatRecord>()
                    Log.i(TAG, "record includes: ${snapshot.documents.size}")

                    for (document in snapshot) {

                        val id = document.get("id").toString()
                        val chatRoomKey = document.get("chat_room_key").toString()
                        val content = document.get("content").toString()
                        val timeStamp = document.get("sent_time").toString()
                        val sender = document.get("sender_name").toString()
                        val type = document.get("message_type").toString()
                        val meetingId = document.get("meeting_id").toString()
                        val isSeen = document.get("is_seen").toString().toBoolean()
                        val meetingOver = document.get("meeting_over").toString().toBoolean()

                        var selfImage = ""
                        var selfName = ""

                        var otherImage = ""
                        var otherName = ""


                        if (document.get("user_a_name").toString() == UserManager.userName) {
                            selfImage = document.get("user_a_img").toString()
                            selfName = document.get("user_a_name").toString()

                            otherImage = document.get("user_b_img").toString()
                            otherName = document.get("user_b_name").toString()
                        } else {
                            otherImage = document.get("user_a_img").toString()
                            otherName = document.get("user_a_name").toString()

                            selfImage = document.get("user_b_img").toString()
                            selfName = document.get("user_b_name").toString()
                        }

                        val timeFormatter = SimpleDateFormat("HH:mm")

                        timeFormatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
                        val date = Date(timeStamp.toLong())
                        val taiwanTime = timeFormatter.format(date)

                        val newRecord = ChatRecord(
                            id = id.toLong(),
                            chatRoomKey = chatRoomKey,
                            content = content,
                            sendTime = taiwanTime,
                            sender = sender,
                            type = type,
                            meetingId = meetingId,
                            otherImage = otherImage,
                            selfImage = selfImage,
                            selfName = selfName,
                            otherName = otherName,
                            isSeen = isSeen,
                            meetingOver = meetingOver

                        )
                        _meetingId = meetingId

                        currentRecord.add(newRecord)
                        updateSeenStatus(room.id, document.id)
                        Log.i(TAG, "get record: ${room.id}")
                    }
                    Log.i(TAG, "current record: $currentRecord")
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

    private fun updateSeenStatus(chatRoomID: String, documentID: String) {
        val chatRoomIsSeenOrNot =
            db.collection("chat_room").document(chatRoomID).collection("chat_record")
                .document(documentID)
        chatRoomIsSeenOrNot.update("is_seen", true)

    }

    private fun saveDataInLocal(record: ChatRecord) {
        reclaimDatabaseDao.insertChatRecord(record)
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
        hourString = if (hourInt < 10) {
            "0$hourInt"
        } else {
            hourInt.toString()
        }

        val timeFormatResult = "$hourString : $minutString"

        return timeFormatResult

    }

    fun searchChatRoomWithFriend(chatRoom: Query) {

        val registration = chatRoom.addSnapshotListener { rooms, error ->
            if (error != null) {
                _recordWithFriend.value = reclaimDatabaseDao.loadAllRecord().toMutableList()
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

        if (_onDestroyed.value == true) {
            registration.remove()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, type: String, meetingId: String = "") {
        val sendTimeInTaiwan = setUpTime()

        val newRecord = ChatRecord(
            id = Random.nextLong(),
            chatRoomKey = chatRoomKey,
            content = text,
            sendTime = System.currentTimeMillis().toString(),
            sender = UserManager.userName,
            type = type,
            meetingId = meetingId,
            otherImage = friendImage,
            selfImage = UserManager.userImage,
            selfName = UserManager.userName,
            otherName = friendName,
            isSeen = false,
            meetingOver = false
        )

        val data = hashMapOf(
            "chat_room_key" to newRecord.chatRoomKey,
            "content" to newRecord.content,
            "sent_time" to newRecord.sendTime,
            "sender_name" to newRecord.sender,
            "id" to newRecord.id,
            "message_type" to type,
            "is_seen" to newRecord.isSeen,
            "meeting_id" to meetingId,
            "user_b_img" to newRecord.otherImage,
            "user_a_img" to UserManager.userImage,
            "user_a_name" to newRecord.selfName,
            "user_b_name" to newRecord.otherName,
            "meeting_over" to newRecord.meetingOver
        )

        val chatRoomCollection =
            db.collection("chat_room").whereEqualTo("key", newRecord.chatRoomKey)
        chatRoomCollection.get().addOnSuccessListener {
            _documentID = it.documents[0].id

            Log.e(TAG, "document ID : $_documentID")

            Log.i(TAG, "current document ID: $_documentID")
            db.collection("chat_room").document(_documentID).collection("chat_record")
                .add(data).addOnSuccessListener {
                    val currentTime = System.currentTimeMillis().toString()
                    updateSentTime(_documentID, newRecord.sendTime, it.id)
                    updateOnFriendList(text, _documentID, newRecord.sendTime)

                }.addOnFailureListener {
                    Log.e(TAG, "error: $it")
                }
        }.addOnFailureListener {
            Log.e(TAG, "cannot add data in chat room")
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpTime(): String {
        val taipeiClock = Clock.system(ZoneId.of("Asia/Taipei"))
        val localTimeNow = LocalTime.now(taipeiClock)

        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        return localTimeNow.format(formatter)

    }

    private fun updateOnFriendList(text: String, chatRoomKey: String, currentTime: String) {
        var currentUnreadTime = 0
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room").document(chatRoomKey)
        chatRoom.get().addOnSuccessListener {
            currentUnreadTime = it.get("unread_times").toString().toInt()
            currentUnreadTime++


            chatRoom.update("last_sentence", text)
            chatRoom.update("send_by_id", UserManager.userId)
            chatRoom.update("sent_time", currentTime)
            chatRoom.update("unread_times", currentUnreadTime)
        }.addOnFailureListener {
            Log.e(TAG, "update unread times failed: $it")
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVideoCallMessage(meetingId: String) {
        val text = "我開啟了視訊，一起加入吧!"
        val type = "videocall"
        sendMessage(text, type, meetingId)
        _meetingId = meetingId


    }

    fun joinMeeting(meetingId: String) {
        _joinMeetingId.value = meetingId
    }

    fun removeListener() {
        _onDestroyed.value = true
    }

    fun turnOffJoinBtn() {
        FirebaseFirestore.getInstance().collection("chat_room").document(_documentID)
            .collection("chat_record").document(_meetingId).update("meeting_over", true).addOnSuccessListener{
                Log.i(TAG, "success turn off meeting")
            }
            .addOnFailureListener {
                Log.e(TAG, "failed to turn off: $it")
            }
    }


}