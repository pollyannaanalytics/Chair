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
import com.google.firebase.firestore.ListenerRegistration
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

    lateinit var recordRegistraion: ListenerRegistration



    init {
        _onDestroyed.value = false
        _recordWithFriend.value = emptyList<ChatRecord>().toMutableList()
        getAllRecordFromRoom(chatRoomKey)

    }


    private fun getAllRecordFromRoom(chatRoomKey: String) {
        Log.i(TAG, "start to get data")

        db.collection("chat_room").whereEqualTo("key", chatRoomKey).get().addOnSuccessListener { documents ->
            val room = documents.documents.get(0)
            _documentID = room.id
            recordRegistraion = room.reference.collection("chat_record")
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
                            Log.i("update isseen", "current seen status is :${newRecord.isSeen}")
                            _meetingId = meetingId

                            currentRecord.add(newRecord)
                            if (!document.get("is_seen").toString().toBoolean()) {
                                Log.i("updateread", "current record is unreaded: ${newRecord.content}")
                                if (newRecord.sender != UserManager.userName) {
                                    Log.i(
                                        "updateread",
                                        "current sender is not me, is ${newRecord.sender}, but i am ${UserManager.userName}"
                                    )
                                    updateSeenStatus(room.id, document.id)
                                } else {
                                    Log.i("updateread", "sender is me")
                                }
                            }

                        }
                        if (room.get("send_by_id").toString() != UserManager.userId && room.get("unread_times").toString() != "0") {
                            Log.i("updateread", "is not sent by me")
                            clearUnreadTimes(room.id)
                        }

                        Log.i(TAG, "current record: $currentRecord")
                        _recordWithFriend.value = currentRecord


                    } else {
                        Log.i(TAG, "record is null")
                    }
                }
        }


    }

    private fun clearUnreadTimes(documentID: String) {

        val chatRoom = db.collection("chat_room")
            .document(documentID)
        chatRoom.get().addOnSuccessListener {
            Log.i(TAG, "update unread time to 0")
            chatRoom.update("unread_times", 0)
        }.addOnFailureListener {
            Log.e(TAG, "failed to clear unread times: $it")
        }

    }

    private fun updateSeenStatus(chatRoomID: String, documentID: String) {
        Log.i("updateread", "update seen status is trigger")
        val chatRoomIsSeenOrNot =
            db.collection("chat_room").document(chatRoomID).collection("chat_record")
                .document(documentID)
        chatRoomIsSeenOrNot.update("is_seen", true)

    }






    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, type: String, meetingId: String = "") {


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
            "is_seen" to false,
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

                    updateOnFriendList(text, _documentID, newRecord.sendTime)

                }.addOnFailureListener {
                    Log.e(TAG, "error: $it")
                }
        }.addOnFailureListener {
            Log.e(TAG, "cannot add data in chat room")
        }


    }



    private fun updateOnFriendList(text: String, chatRoomKey: String, currentTime: String) {
        var currentUnreadTime = 0
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room").document(chatRoomKey)
        chatRoom.get().addOnSuccessListener {
            currentUnreadTime = it.get("unread_times").toString().toInt()
            currentUnreadTime++

            val updateData = mapOf<String, String>(
                "key" to chatRoomKey,
                "last_sentence" to text,
                "send_by_id" to UserManager.userId,
                "sent_time" to currentTime,
                "unread_times" to currentUnreadTime.toString()

            )
            chatRoom.update(updateData)
            Log.i("updateread", "successfullly")
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
        Log.i("update", "ondestroyed is true")
    }

    fun turnOffJoinBtn() {

        Log.i(TAG, "document: $_documentID, chat_record: $_meetingId")
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room").document(_documentID)
        var meetingDocumentID = ""
        chatRoom.collection("chat_record").whereEqualTo("meeting_id", _meetingId).get()
            .addOnSuccessListener {

                meetingDocumentID = it.documents.get(0).id

                val meetingDocument = chatRoom.collection("chat_record").document(meetingDocumentID)

                meetingDocument.update("meeting_over", true)
                meetingDocument.update("content", "通話已結束")
                chatRoom.update("last_sentence", "通話已結束")

            }

    }

    override fun onCleared() {
        Log.i(TAG, "registration is cleared")
        recordRegistraion.remove()

    }
}