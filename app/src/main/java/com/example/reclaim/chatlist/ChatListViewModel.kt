package com.example.reclaim.chatlist

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ChatRoomLocal
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


private const val TAG = "ChatListViewModel"

@RequiresApi(Build.VERSION_CODES.M)
class ChatListViewModel(private val reclaimDao: ReclaimDatabaseDao, val activity: Activity) :
    ViewModel() {
    private var _friendsList = MutableLiveData<MutableList<Friends>>()
    val friendsList: LiveData<MutableList<Friends>>
        get() = _friendsList
    val db = Firebase.firestore

    private var _recordList = MutableLiveData<MutableList<ChatRoom>>()
    val recordList: LiveData<MutableList<ChatRoom>>
        get() = _recordList

    private var _navigateToChatRoom = MutableLiveData<ChatRoom?>()
    val navigateToChatRoom: LiveData<ChatRoom?>
        get() = _navigateToChatRoom

    val userManager = UserManager

    init {
        _friendsList.value = emptyList<Friends>().toMutableList()
        _recordList.value = emptyList<ChatRoom>().toMutableList()
        loadAllRecordsFromFirebase()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetworkConnection() {
        val hasInternetConnection = hasInternetConnection()
        if (hasInternetConnection) {
            loadAllRecordsFromFirebase()
        } else {
//            loadAllRecordFromLocal()
        }
    }

//    private fun loadAllRecordFromLocal() {
//        viewModelScope.launch {
//            _recordList.value = reclaimDao.loadAllChatRoom().toMutableList()
//        }
//    }


    private fun loadAllRecordsFromFirebase() {
        db.collection("chat_room").where(
            Filter.or(
                Filter.equalTo("user_a_name", UserManager.userName),
                Filter.equalTo("user_b_name", UserManager.userName)
            )
        )
            .orderBy("last_sentence", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "cannot load record: $error")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (!snapshot.isEmpty) {
                        var currentChatRoom = emptyList<ChatRoom>().toMutableList()
                        for (query in snapshot) {

                            Log.i(TAG, "total record: " + snapshot.size().toString())

                            val key = query.data.get("key").toString()
                            val sentTime = query.data.get("sent_time").toString()
                            val id = query.id
                            val unreadTimes = query.data.get("unread_times").toString()

                            var selfId = ""
                            var selfName = ""
                            var selfImage = ""

                            var otherId = ""
                            var otherName = ""
                            var otherImage = ""
                            var otherOnline = true
                            var lastSentence = ""
                            var sendById = ""

                            lastSentence = query.data.get("last_sentence").toString()
                            sendById = query.data.get("send_by_id").toString()


                            val timeFormatter = SimpleDateFormat("HH:mm")

                            timeFormatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
                            val date = Date(sentTime.toLong())
                            val taiwanTime = timeFormatter.format(date)


                            if (query.data.get("user_a_id").toString() == UserManager.userId) {
                                selfId = query.data.get("user_a_id").toString()
                                otherId = query.data.get("user_b_id").toString()
                                selfName = query.data.get("user_a_name").toString()
                                otherName = query.data.get("user_b_name").toString()
                                selfImage = query.data.get("user_a_img").toString()
                                otherImage = query.data.get("user_b_img").toString()
                                otherOnline = query.data.get("user_b_online").toString().toBoolean()

                            } else {
                                otherId = query.data.get("user_a_id").toString()
                                selfId = query.data.get("user_b_id").toString()

                                otherName = query.data.get("user_a_name").toString()
                                selfName = query.data.get("user_b_name").toString()

                                otherImage = query.data.get("user_a_img").toString()
                                selfImage = query.data.get("user_b_img").toString()
                                otherOnline = query.data.get("user_b_online").toString().toBoolean()
                            }

                            val newRecord = ChatRoom(
                                id,
                                key,
                                selfId,
                                otherId,
                                selfName,
                                otherName,
                                lastSentence,
                                sendById,
                                selfImage,
                                otherImage,
                                taiwanTime,
                                unreadTimes,
                                otherOnline
                            )

                            currentChatRoom.add(newRecord)


                            val saveChatRoomInLocal = ChatRoomLocal(
                                selfId = newRecord.selfId,
                                otherId = newRecord.otherId,
                                selfName = newRecord.selfName,
                                otherName = newRecord.otherName,
                                lastSentence = newRecord.lastSentence,
                                sendById = newRecord.sendById,
                                selfImage = newRecord.selfImage,
                                otherImage = newRecord.otherImage
                            )
                            viewModelScope.launch {
                                reclaimDao.insertChatRoom(saveChatRoomInLocal)
                                Log.i(TAG, "new record is $newRecord")
                            }

                        }
                        _recordList.value = currentChatRoom

                        Log.i(TAG, "current list is ${_recordList.value}")
                    } else {
                        Log.i(TAG, "chat room is empty")
                    }
                }


            }


    }


    fun displayChatRoom(data: ChatRoom) {
        _navigateToChatRoom.value = data
        clearUnreadTimes(documentID = data.id)
    }

    fun navigateToRoom() {
        _navigateToChatRoom.value = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = activity.application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false

        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true

            else -> false
        }
    }

    private fun clearUnreadTimes(documentID: String) {
        val chatRoom = db.collection("chat_room")
            .document(documentID)
        chatRoom.get().addOnSuccessListener {
            if (it.get("send_by_id").toString() != UserManager.userId){
                chatRoom.update("unread_times", 0)
            }else{
                Log.i(TAG, "newest message is send by me")
            }
        }
    }


}