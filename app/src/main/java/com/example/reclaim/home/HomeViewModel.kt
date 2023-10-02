package com.example.reclaim.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.Direction
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "HOMEVIEWMODEL"

class HomeViewModel(private val reclaimDatabaseDao: ReclaimDatabaseDao) : ViewModel() {

    private var _otherProfileList = MutableLiveData<MutableList<UserProfile>>()
    val otherProfileList: LiveData<MutableList<UserProfile>>
        get() = _otherProfileList

    private var _matchToChatRoom = MutableLiveData<ChatRoom?>()
    val matchToChatRoom: LiveData<ChatRoom?>
        get() = _matchToChatRoom


    private var _firebaseDisconnect = MutableLiveData<Boolean>(false)
    val firebaseDisconnect: LiveData<Boolean>
        get() = _firebaseDisconnect

    private var _noFriends = MutableLiveData<Boolean>(false)
    val noFriends: LiveData<Boolean>
        get() = _noFriends

    val db = Firebase.firestore


    private var _onDestroyed = MutableLiveData<Boolean>(false)
    val onDestroyed: LiveData<Boolean>
        get() = _onDestroyed


    private var currentFriends = emptyList<String>().toMutableList()

    val userManager = UserManager


    init {
        Log.i(TAG, "viewModel start")
        _onDestroyed.value = false
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()
        currentFriends = loadWhoLikeMe(UserManager.userId)

    }

    private fun loadOtherProfile() {
        if (currentFriends.size != 0) {
            Log.i(TAG, "currentFriend: $currentFriends")
            try {
                val otherResultDocument =
                    db.collection("user_profile")
                        .whereEqualTo("worries_type", UserManager.userType)
                        .whereNotIn("user_id", currentFriends)
                        .orderBy("user_id", Query.Direction.DESCENDING)
                        .orderBy("profile_time", Query.Direction.DESCENDING)


                val registration = otherResultDocument?.addSnapshotListener { querysnapshot, e ->
                    if (e != null) {
                        _firebaseDisconnect.value = true
                        Log.i(TAG, e.toString())
                    }
                    _otherProfileList.value?.clear()


                    if (querysnapshot!!.documents.size != 0) {
                        Log.i(TAG, "current all profile size: ${querysnapshot.documents.size}")
                        Log.i(TAG, "querysnapshot is not empty, start to get firebase")
                        getFieldFromFirebase(querysnapshot)
                    } else {
                        loadAllUsers()
                        Log.i(TAG, "no similar, start to load all")
                    }
                }

                if (_onDestroyed.value == true) {
                    registration?.remove()
                }

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        } else {
            try {
                Log.i(TAG, "currentFriend: $currentFriends")
                val otherResultDocument =
                    db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                        .whereEqualTo("worries_type", UserManager.userType)
                        .orderBy("user_id", Query.Direction.DESCENDING)
                        .orderBy("profile_time", Query.Direction.DESCENDING)


                val registration = otherResultDocument?.addSnapshotListener { querysnapshot, e ->
                    if (e != null) {
                        _firebaseDisconnect.value = true
                        Log.i(TAG, e.toString())
                    }
                    _otherProfileList.value?.clear()


                    if (querysnapshot!!.documents.size != 0) {
                        Log.i(TAG, "querysnapshot is not empty, start to get firebase")
                        Log.i(TAG, "current all size: ${querysnapshot.documents.size}")
                        getFieldFromFirebase(querysnapshot)
                    } else {
                        loadAllUsers()
                        Log.i(TAG, "no similar, start to load all")
                    }
                }

                if (_onDestroyed.value == true) {
                    registration?.remove()
                }

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        }


    }

    private fun getFieldFromFirebase(querysnapshot: QuerySnapshot?) {
        val currentList = emptyList<UserProfile>().toMutableList()
        try {
            Log.i(TAG, querysnapshot!!.documents.size.toString())
            _noFriends.value = false
            for (snapshot in querysnapshot.documents) {
                val userId = snapshot.data?.get("user_id").toString()
                val userName = snapshot.data?.get("user_name").toString()
                val userGender = snapshot.data?.get("gender").toString()
                val worriesDescription = snapshot.data?.get("worries_description").toString()
                val worriesType = snapshot.data?.get("worries_type").toString()
                val images = snapshot.data?.get("images").toString()

                val newUser = UserProfile(
                    userId = userId,
                    userName = userName,
                    gender = userGender,
                    worriesDescription = worriesDescription,
                    worryType = worriesType,
                    imageUri = images
                )

                currentList.add(newUser)
                _otherProfileList.value = currentList


                Log.i(TAG, "newUser is $newUser")
            }

            _firebaseDisconnect.value = false

        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            _firebaseDisconnect.value = true
        }
    }

    private fun loadAllUsers() {
        var otherProfileResult: Query? = null

        if (currentFriends.size != 0) {

            otherProfileResult =
                db.collection("user_profile").whereNotIn("user_id", currentFriends)
                    .orderBy("user_id", Query.Direction.DESCENDING)
                    .orderBy("profile_time", Query.Direction.DESCENDING)


            val registration = otherProfileResult.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    _firebaseDisconnect.value = true
                }
                if (querySnapshot != null) {
                    if (querySnapshot.documents.size != 0) {
                        getFieldFromFirebase(querySnapshot)
                    } else {
                        _noFriends.value = true
                    }
                } else {
                    _noFriends.value = true
                    Log.i(TAG, "no user")
                }


            }

            if (_onDestroyed.value == true) {
                registration.remove()
            }


        } else {
            otherProfileResult =
                db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                    .orderBy("user_id", Query.Direction.DESCENDING)


            val registration = otherProfileResult.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    _firebaseDisconnect.value = true
                    Log.e(TAG, "load all fail: $e")
                }
                if (querySnapshot != null) {
                    if (querySnapshot.documents.size != 0) {
                        getFieldFromFirebase(querySnapshot)
                    } else {
                        _noFriends.value = true
                    }
                } else {
                    _noFriends.value = true
                    Log.i(TAG, "no user")
                }


            }
            registration

            if (_onDestroyed.value == true) {
                registration.remove()
            }
        }


    }

    private fun loadWhoLikeMe(userId: String): MutableList<String> {
        Log.i(TAG, "start to load who is friend currently")
        var shouldRemove = false
        val currentFriendList = emptyList<String>().toMutableList()

        currentFriendList.add(UserManager.userId)
        currentFriends.add(UserManager.userId)
        val registration = db.collection("relationship")
            .where(
                Filter.or(
                    Filter.equalTo("receiver_id", UserManager.userId),
                    Filter.equalTo("sender_id", UserManager.userId)
                )
            ).where(
                Filter.or(
                    Filter.equalTo("current_relationship", "Like"),
                    Filter.equalTo("current_relationship", "Dislike")
                )
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "cannot load current friend: $error")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (shot in snapshot) {
                        if (shot.data["receiver_id"].toString() == UserManager.userId) {
                            Log.i(TAG, "current receiver: ${UserManager.userId}")
                            currentFriendList.add(shot.data["sender_id"].toString())
                        } else {
                            currentFriendList.add(shot.data["receiver_id"].toString())
                        }
                        Log.i(TAG, "currentFriendList: $currentFriendList")
                        shouldRemove = true

                    }
                    loadOtherProfile()
                } else {
                    Log.e("findFriends", "no friends")
                    loadOtherProfile()
                }


            }
        registration


        if (shouldRemove) {
            registration.remove()
        }



        return currentFriendList

    }

    fun likeOrDislike(
        friendId: String,
        friendName: String,
        friendImg: String,
        likeOrDislike: String,
        documentId: String
    ) {
        Log.i(TAG, documentId)
        val reference = db.collection("relationship").document(documentId)
        if (likeOrDislike == "Dislike") {
            reference.update("current_relationship", likeOrDislike)
            Log.i(TAG, "update relationship: $likeOrDislike")
        }

        if (likeOrDislike == "Like") {
            reference.get().addOnSuccessListener {
                val currentRelationship = it.get("current_relationship")
                if (currentRelationship == "Pending") {
                    Log.i(TAG, "like, to create room")
                    reference.update("current_relationship", likeOrDislike)
                    createAChatRoom(friendId, friendName, friendImg, documentId)
                } else {
                    Log.i(TAG, "another user dislike, not to create room")
                }
            }.addOnFailureListener {
                Log.e(TAG, "cannot get relationship: $it")
            }
        }
    }



    private fun createAChatRoom(
        friendId: String,
        friendName: String,
        friendImg: String,
        documentId: String
    ): String {
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room")

        val data = hashMapOf(
            "key" to friendId + UserManager.userId,
            "user_a_id" to UserManager.userId,
            "user_a_name" to UserManager.userName,
            "user_b_id" to friendId,
            "user_b_name" to friendName,
            "last_sentence" to "",
            "sent_time" to "",
            "send_by_id" to "",
            "user_a_img" to UserManager.userImage,
            "user_b_img" to friendImg,
            "unread_times" to 0

        )
        var currentRoomKey = friendId + UserManager.userId


        chatRoom.add(data).addOnSuccessListener {
            updateChatRoomKeyInRelationship(currentRoomKey, documentId)
            _matchToChatRoom.value = ChatRoom(
                currentRoomKey,
                UserManager.userId,
                friendId,
                UserManager.userName,
                friendName,
                "",
                "",
                UserManager.userImage,
                friendImg,
                ""
            )

        }.addOnFailureListener {
            Log.e(TAG, "create room failed: $it")

        }



        return currentRoomKey
    }

    private fun updateChatRoomKeyInRelationship(currentRoomKey: String, relationshipId: String) {
        val reference = db.collection("relationship").document(relationshipId)
        reference.update("chat_room_key", currentRoomKey)
            .addOnSuccessListener {
                Log.i(TAG, "DocumentSnapshot successfully updated!")
            }.addOnFailureListener {
                Log.w(TAG, "Error updating document", it)
            }

    }


    fun findRelationship(
        friendId: String,
        friendName: String,
        friendImg: String,
        direction: Direction
    ) {
        Log.i(TAG, "start to find relationship")

        db.collection("relationship").where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("sender_id", UserManager.userId),
                    Filter.equalTo("receiver_id", friendId)
                ),
                Filter.and(
                    Filter.equalTo("sender_id", friendId),
                    Filter.equalTo("receiver_id", UserManager.userId)
                )
            )
        ).orderBy("receiver_id", Query.Direction.DESCENDING).get().addOnSuccessListener {
            if (!it.isEmpty) {
                Log.i(TAG, "relationship is not null")
                for (query in it) {
                    if (query.data.get("chat_room_key") == "null") {
                        updateRelationShip(
                            friendId,
                            friendName,
                            friendImg,
                            direction,
                            query.id
                        )

                    } else {
                        Log.i(TAG, "there is already room key")

                    }
                }
            } else {
                createRelationShip(friendId, friendName, direction)
                Log.i(TAG, "there is no relationship, start to create")
            }

        }.addOnFailureListener {
            Log.i(TAG, "find relationship fail: $it")
        }


    }

    private fun createRelationShip(friendId: String, friendName: String, direction: Direction) {
        Log.i(TAG, "there is no friend, start to create")
        val friends = FirebaseFirestore.getInstance().collection("relationship")
        val relationship = when (direction) {
            Direction.Left -> "Dislike"
            Direction.Right -> "Pending"
            else -> "Pending"
        }

        val data = hashMapOf(
            "receiver_id" to friendId,
            "receiver_name" to friendName,
            "sender_id" to UserManager.userId,
            "sender_name" to UserManager.userName,
            "current_relationship" to relationship,
            "chat_room_key" to "null",

            )

        friends.add(data)
            .addOnSuccessListener {
                Log.i(TAG, "create relationship success")
            }.addOnFailureListener {
                Log.i(TAG, "create relationship failed: $it")
            }


    }

    private fun updateRelationShip(
        friendId: String,
        friendName: String,
        friendImg: String,
        direction: Enum<Direction>,
        documentId: String
    ) {
        Log.i(TAG, "friendId is $friendId")
        when (direction) {
            Direction.Left -> likeOrDislike(friendId, friendName, friendImg, "Dislike", documentId)
            Direction.Right -> {
                likeOrDislike(friendId, friendName, friendImg, "Like", documentId)
                Log.i(TAG, "Liked friendId is $friendId")
            }

            else -> likeOrDislike(friendId, friendName, friendImg, "Like", documentId)
        }
    }


    fun removeProfileListener() {
        _onDestroyed.value = true
    }

    fun navigateToMatch() {
        _matchToChatRoom.value = null
    }
}