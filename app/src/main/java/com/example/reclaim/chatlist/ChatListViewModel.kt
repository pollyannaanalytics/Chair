package com.example.reclaim.chatlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private const val TAG = "CHATLISTVIEWMODEL"

class ChatListViewModel(private val dao: ReclaimDatabaseDao) : ViewModel() {
    private var _friendsList = MutableLiveData<MutableList<Friends>>()
    val friendsList: LiveData<MutableList<Friends>>
        get() = _friendsList
    val db = Firebase.firestore

    private var _navigateToChatRoom = MutableLiveData<Friends>()
    val navigateToChatRoom : LiveData<Friends>
        get() = _navigateToChatRoom

    init {
        _friendsList.value = emptyList<Friends>().toMutableList()
        loadAllMatch()
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
                .orderBy("sender_id", com.google.firebase.firestore.Query.Direction.DESCENDING)

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
                        if(senderId == UserManager.userId){
                            val newFriend = receiverId
                            Log.i(TAG, "new friend is $newFriend")
                            currentFriendsList.add(newFriend.toString())
                            searchFriendsInfoFromFirebase(newFriend.toString(), chatRoomKey)
                            Log.i(TAG, "friends include $currentFriendsList")
                        }else{
                            val newFriend = senderId
                            Log.i(TAG, "new friend is $newFriend")
                            currentFriendsList.add(newFriend.toString())
                            searchFriendsInfoFromFirebase(newFriend.toString(),chatRoomKey)
                            Log.i(TAG, "friends include ${currentFriendsList}")
                        }
                    }


                }else{
                    Log.i(TAG, "no one add this person")
                }
            }
        } catch (e: Exception) {

        }


    }

//    private fun saveFriendListInLocal(friends: MutableList<Friends>) {
//
//        viewModelScope.launch {
//            dao.saveFriendList(friends)
//        }
//
//    }

    private fun searchFriendsInfoFromFirebase(friendId: String, chatRoomKey: String) {
        val currentFriendList = emptyList<Friends>().toMutableList()
        val searchFriendProfile = db.collection("user_profile").whereEqualTo("user_id", friendId).orderBy("user_name", Query.Direction.DESCENDING)

        searchFriendProfile.addSnapshotListener { querySnapShot, error ->
            if (error != null){
                Log.e(TAG, error.toString())
                return@addSnapshotListener
            }

            if(querySnapShot != null &&  !querySnapShot.metadata.hasPendingWrites()){
                currentFriendList.clear()
                _friendsList.value!!.clear()
                for (snapshot in querySnapShot){
                    val userId = snapshot.data?.get("user_id").toString()
                    val userName = snapshot.data?.get("user_name").toString()
                    val imageInArray = listOf<String>(snapshot.data.get("images").toString())
                    val mainImage = imageInArray[0].removeSurrounding("[", "]")
                    val friendInfo = Friends(userId = userId, userName = userName, imageUri = mainImage, chatRoomKey = chatRoomKey)
                    currentFriendList.add(friendInfo)
                }

//                saveFriendListInLocal(currentFriendList)
                Log.i(TAG, currentFriendList.toString())
                _friendsList.value = currentFriendList
            }
        }
    }


    fun displayChatRoom(data: Friends){
        _navigateToChatRoom.value = data
    }




}