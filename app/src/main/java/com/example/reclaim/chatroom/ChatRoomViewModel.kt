package com.example.reclaim.chatroom

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.MessageType
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.source.ChairRepository
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

const val TAG = "ChatRoomViewModel"

class ChatRoomViewModel(
    private val chairRepository: ChairRepository,
    private val navArgs: ChatRoomFragmentArgs,
    private val userName: String = UserManager.userName,
    private val userImageUri: String = UserManager.userImage
) :
    ViewModel() {
    companion object {
        private const val TAG = "ChatRoomViewModel"
    }


    private val chatRoom = navArgs.chatRoom
    private val chatRoomKey = navArgs.chatRoom.key
    val friendImage = navArgs.chatRoom.otherImage
    val friendName = navArgs.chatRoom.otherName

    private var _recordWithFriend = MutableLiveData<MutableList<ChatRecord>>()
    val recordWithFriend: LiveData<MutableList<ChatRecord>>
        get() = _recordWithFriend

    private var meetingID = ""
    private var chatRoomDocumentID = ""
    private var chatRecordDocumentID =""




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


    fun getAllRecordFromRoom(chatRoomKey: String) {
//        Log.i(TAG, "start to get data")

        chairRepository.getAllRecordFromRoom(chatRoomKey) { query, room ->
            if (!room.exists()){
                return@getAllRecordFromRoom
            }

            chatRoomDocumentID = room.id

            recordRegistraion = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "this is error: ${error.message}")


                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentRecord = mutableListOf<ChatRecord>()
                    Log.i(TAG, "record includes: ${snapshot.documents.size}")

                    for (document in snapshot) {

                        val documentID = document.get("id").toString()
                        chatRecordDocumentID = documentID


                        val chatRoomKey = document.get("chat_room_key").toString()
                        val content = document.get("content").toString()
                        val timeStamp = document.get("sent_time").toString()
                        val sender = document.get("sender_name").toString()
                        val type = document.get("message_type").toString()
                        val meetingId = document.get("meeting_id").toString()
                        val isSeen = document.get("is_seen").toString().toBoolean()
                        val meetingOver = document.get("meeting_over").toString().toBoolean()


                        val messageType = when(type){
                            "MESSAGE" -> MessageType.MESSAGE
                            "VIDEO_CALL" -> MessageType.VIDEO_CALL
                            else -> {MessageType.MESSAGE}
                        }


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

                            chatRoomKey = chatRoomKey,
                            content = content,
                            sendTime = taiwanTime,
                            sender = sender,
                            type = messageType,
                            meetingId = meetingId,
                            otherImage = otherImage,
                            selfImage = selfImage,
                            selfName = selfName,
                            otherName = otherName,
                            isSeen = isSeen,
                            meetingOver = meetingOver

                        )
                        Log.i("update isseen", "current seen status is :${newRecord.isSeen}")
                        meetingID = meetingId

                        currentRecord.add(newRecord)
                        if (!document.get("is_seen").toString().toBoolean()) {
                            Log.i("updateread", "current record is unreaded: ${newRecord.content}")
                            if (newRecord.sender != UserManager.userName) {
                                Log.i(
                                    "updateread",
                                    "current sender is not me, is ${newRecord.sender}, but i am ${UserManager.userName}"
                                )
                                updateSeenStatus(room.id, documentID)
                            } else {
                                Log.i("updateread", "sender is me")
                            }
                        }

                    }
                    if (room.get("send_by_id")
                            .toString() != UserManager.userId && room.get("unread_times")
                            .toString() != "0"
                    ) {
                        Log.i("updateread", "is not sent by me")
                        clearUnreadCounts(room.id)
                    }

                    Log.i(TAG, "current record: $currentRecord")
                    _recordWithFriend.value = currentRecord


                } else {
                    Log.i(TAG, "record is null")
                }
            }
        }
    }

    fun clearUnreadCounts(documentID: String) {
        chairRepository.clearUnreadCounts(documentID)

    }

    fun updateSeenStatus(chatRoomID: String, documentID: String) {
        chairRepository.updateSeenStatus(chatRoomID, documentID)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(content: String, type: MessageType, meetingId: String = "") {
        chairRepository.sendMessage(content, type, meetingId, chatRoom, chatRoomDocumentID, userName, userImageUri)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVideoCallMessage(meetingId: String) {
        chairRepository.sendVideoCallMessage(meetingId, chatRoom, chatRoomDocumentID, userName, userImageUri)

    }

    fun joinMeeting(meetingId: String) {
        _joinMeetingId.value = meetingId
    }

    fun removeListener() {
        _onDestroyed.value = true
        Log.i("update", "ondestroyed is true")
    }

    fun stopUserJoinMeeting() {
//        recordRegistraion.remove()
       chairRepository.stopUserJoinMeeting(chatRoomDocumentID, meetingID)

    }

    override fun onCleared() {
        Log.i(TAG, "registration is cleared")
        recordRegistraion.remove()

    }
}