package com.example.reclaim.chatlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "ChatListViewModel"

class ChatListViewModel(private val dao: ReclaimDatabaseDao) : ViewModel() {
    private var _friendsList = MutableLiveData<MutableList<Friends>>()
    val friendsList: LiveData<MutableList<Friends>>
        get() = _friendsList
    val db = Firebase.firestore

    private var _recordList = MutableLiveData<MutableList<ChatRoom>>()
    val recordList: LiveData<MutableList<ChatRoom>>
        get() = _recordList

    private var _navigateToChatRoom = MutableLiveData<Friends>()
    val navigateToChatRoom: LiveData<Friends>
        get() = _navigateToChatRoom

    init {
        _friendsList.value = emptyList<Friends>().toMutableList()
        _recordList.value = emptyList<ChatRoom>().toMutableList()
        loadAllMatch()
//        loadAllRecords()
    }

    fun loadAllRecords() {
        db.collection("chat_room").where(
            Filter.or(
                Filter.equalTo("user_a_name", UserManager.userName),
                Filter.equalTo("user_b_name", UserManager.userName)
            )
        ).whereNotEqualTo("last_sentence", "")
            .orderBy("last_sentence", Query.Direction.ASCENDING)
            .orderBy("sent_time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null){
                    Log.e(TAG, "cannot load record: $error")
                    return@addSnapshotListener
                }

                if(snapshot != null){
                    var currentChatRoom = emptyList<ChatRoom>().toMutableList()
                    for (query in snapshot){
                        Log.i(TAG, "total record: " + snapshot.size().toString())
                        val key = query.data.get("key").toString()
                        val userAId = query.data.get("user_a_id").toString()
                        val userBId = query.data.get("user_b_id").toString()
                        val userAName = query.data.get("user_a_name").toString()
                        val userBName = query.data.get("user_b_name").toString()
                        val lastSentence = query.data.get("last_sentence").toString()
                        val sendById = query.data.get("send_by_id").toString()
                        val userAImg = query.data.get("send_by_img").toString()
                        val userBImg = query.data.get("userBImg").toString()

                        val newRecord = ChatRoom(key, userAId, userBId, userAName, userBName, lastSentence, sendById, userAImg, userBImg)
                        currentChatRoom.add(newRecord)
                        _recordList.value = currentChatRoom
                        Log.i(TAG, "new record is $newRecord")
                    }


                    Log.i(TAG, "current list is ${_recordList.value}")
                }


            }


    }

    fun loadAllMatch() {

        try {
            var currentFriendsList = emptyList<String>().toMutableList()

            val allMatchDocument = db.collection("relationship").where(
                Filter.or(
                    Filter.equalTo("receiver_id", UserManager.userId),
                    Filter.equalTo("sender_id", UserManager.userId)
                )
            )
                .whereEqualTo("current_relationship", "Like")
                .orderBy("sender_id", Query.Direction.DESCENDING)

            allMatchDocument.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.e(TAG, "$e")
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        Log.i(TAG, "document number is ${document.data.get("receiver_id")}")
                        val senderId = document.data.get("sender_id")
                        val receiverId = document.data.get("receiver_id")
                        val chatRoomKey = document.data.get("chat_room_key").toString()
                        if (senderId == UserManager.userId) {
                            val newFriend = receiverId
                            Log.i(TAG, "new friend is $newFriend")
                            currentFriendsList.add(newFriend.toString())
                            searchFriendsInfoFromFirebase(newFriend.toString(), chatRoomKey)
                            Log.i(TAG, "friends include $currentFriendsList")
                        } else {
                            val newFriend = senderId
                            Log.i(TAG, "new friend is $newFriend")
                            currentFriendsList.add(newFriend.toString())
                            searchFriendsInfoFromFirebase(newFriend.toString(), chatRoomKey)
                            Log.i(TAG, "friends include ${currentFriendsList}")
                        }
                    }


                } else {
                    Log.i(TAG, "no one add this person")
                }
            }
        } catch (e: Exception) {

        }


    }


    private fun searchFriendsInfoFromFirebase(friendId: String, chatRoomKey: String) {
        val currentFriendList = emptyList<Friends>().toMutableList()
        val searchFriendProfile = db.collection("user_profile").whereEqualTo("user_id", friendId)
            .orderBy("user_name", Query.Direction.DESCENDING)

        searchFriendProfile.addSnapshotListener { querySnapShot, error ->
            if (error != null) {
                Log.e(TAG, error.toString())
                return@addSnapshotListener
            }

            if (querySnapShot != null && !querySnapShot.metadata.hasPendingWrites()) {
                currentFriendList.clear()
                _friendsList.value!!.clear()
                for (snapshot in querySnapShot) {
                    val userId = snapshot.data?.get("user_id").toString()
                    val userName = snapshot.data?.get("user_name").toString()
                    val imageInArray = listOf<String>(snapshot.data.get("images").toString())
                    val mainImage = imageInArray[0].removeSurrounding("[", "]")
                    val friendInfo = Friends(
                        userId = userId,
                        userName = userName,
                        imageUri = mainImage,
                        chatRoomKey = chatRoomKey
                    )
                    currentFriendList.add(friendInfo)
                }

                Log.i(TAG, currentFriendList.toString())
                _friendsList.value = currentFriendList
            }
        }
    }


    fun displayChatRoom(data: Friends) {
        _navigateToChatRoom.value = data
    }


}