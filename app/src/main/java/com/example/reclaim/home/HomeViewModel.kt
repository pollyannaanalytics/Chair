package com.example.reclaim.home

import android.util.Log
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

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.Direction

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


    val userManager = UserManager


    init {
        Log.i(TAG, "viewModel start")
        _onDestroyed.value = false
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()


    }

    private fun loadOtherProfile(currentFriends: List<String>) {
        Log.i(TAG, "current friends: ${currentFriends}")
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()
        if (currentFriends.isNotEmpty()) {
            Log.i(TAG, "currentFriend: $currentFriends")
            try {
                val otherResultDocument =
                    db.collection("user_profile")
                        .whereEqualTo("worries_type", UserManager.userType)
                        .whereNotIn("user_id", currentFriends)
                        .orderBy("user_id", Query.Direction.ASCENDING)
                        .orderBy("profile_time", Query.Direction.DESCENDING)


                val registration =
                    otherResultDocument?.get()?.addOnSuccessListener { querysnapshot ->

                        if (querysnapshot != null && querysnapshot!!.documents.size != 0) {
                            val currentList = emptyList<UserProfile>().toMutableList()

                            Log.i(
                                TAG,
                                "current all profile size: ${querysnapshot?.documents?.size}"
                            )
                            Log.i(TAG, "querysnapshot is not empty, start to get firebase")
                            for (snapshot in querysnapshot) {

                                val gender = snapshot.get("gender").toString()
                                val images = snapshot.get("images").toString()
                                val profileTime = snapshot.get("profile_time").toString()
                                val userId = snapshot.get("user_id").toString()
                                val userName = snapshot.get("user_name").toString()
                                val worriesDescription =
                                    snapshot.get("worries_description").toString()
                                val worriesType = snapshot.get("worries_type").toString()
                                val age = snapshot.get("user_age").toString()
                                val selfDescription = snapshot.get("self_description").toString()

                                val newUserProfile = UserProfile(
                                    userId = userId,
                                    userName = userName,
                                    gender = gender,
                                    worryType = worriesType,
                                    worriesDescription = worriesDescription,
                                    imageUri = images,
                                    age = age,
                                    selfDescription = selfDescription
                                )

                                Log.i(TAG, "load real profile: ${newUserProfile.userId}")
                                currentList.add(newUserProfile)
                            }
                            _otherProfileList.value = currentList

                        } else {
                            if (querysnapshot == null) {
                                Log.i(TAG, "currently is null")
                                _noFriends.value = true
                            } else {
                                loadAllUsers(currentFriends)
                                Log.i(TAG, "no similar, start to load all")
                            }

                        }
                    }?.addOnFailureListener {
                        Log.i(TAG, it.toString())
                    }
                registration


            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        } else {
            try {
                Log.i(TAG, "currentFriend: $currentFriends")
                val currentList = emptyList<UserProfile>().toMutableList()

                val otherResultDocument =
                    db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                        .whereEqualTo("worries_type", UserManager.userType)
                        .orderBy("user_id", Query.Direction.DESCENDING)
                        .orderBy("profile_time", Query.Direction.DESCENDING)


                val registration =
                    otherResultDocument?.get()?.addOnSuccessListener { querysnapshot ->

                        if (querysnapshot!!.documents.size != 0) {
                            Log.i(TAG, "querysnapshot is not empty, start to get firebase")
                            Log.i(TAG, "current all size: ${querysnapshot.documents.size}")

                            for (snapshot in querysnapshot) {
                                val gender = snapshot.get("gender").toString()
                                val images = snapshot.get("images").toString()
                                val profileTime = snapshot.get("profile_time").toString()
                                val userId = snapshot.get("user_id").toString()
                                val userName = snapshot.get("user_name").toString()
                                val worriesDescription =
                                    snapshot.get("worries_description").toString()
                                val worriesType = snapshot.get("worries_type").toString()
                                val age = snapshot.get("user_age").toString()
                                val selfDescription = snapshot.get("self_description").toString()

                                val newUserProfile = UserProfile(
                                    userId = userId,
                                    userName = userName,
                                    gender = gender,
                                    worryType = worriesType,
                                    worriesDescription = worriesDescription,
                                    imageUri = images,
                                    age = age,
                                    selfDescription = selfDescription
                                )
                                currentList.add(newUserProfile)
                            }
                            _otherProfileList.value = currentList

                        } else {
                            loadAllUsers(currentFriends)
                            Log.i(TAG, "no similar, start to load all")
                        }
                    }?.addOnFailureListener {
                        Log.i(TAG, "load all users failed: $it")
                    }
                registration

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        }


    }


    private fun loadAllUsers(currentFriends: List<String>) {
        var otherProfileResult: Query? = null

        if (currentFriends.isNotEmpty()) {
            Log.i(TAG, "load all, current list: $currentFriends")

            otherProfileResult =
                db.collection("user_profile").whereNotIn("user_id", currentFriends)
                    .orderBy("user_id", Query.Direction.DESCENDING)
                    .orderBy("profile_time", Query.Direction.DESCENDING)

            val registration = otherProfileResult.get().addOnSuccessListener { querySnapshot ->
                val currentList = emptyList<UserProfile>().toMutableList()
                if (querySnapshot != null) {
                    if (querySnapshot.documents.size != 0) {
                        for (snapshot in querySnapshot) {
                            val gender = snapshot.get("gender").toString()
                            val images = snapshot.get("images").toString()
                            val profileTime = snapshot.get("profile_time").toString()
                            val userId = snapshot.get("user_id").toString()
                            val userName = snapshot.get("user_name").toString()
                            val worriesDescription =
                                snapshot.get("worries_description").toString()
                            val worriesType = snapshot.get("worries_type").toString()
                            val age = snapshot.get("user_age").toString()
                            val selfDescription = snapshot.get("self_description").toString()

                            val newUserProfile = UserProfile(
                                userId = userId,
                                userName = userName,
                                gender = gender,
                                worryType = worriesType,
                                worriesDescription = worriesDescription,
                                imageUri = images,
                                age = age,
                                selfDescription = selfDescription
                            )

                            currentList.add(newUserProfile)
                        }
                        _otherProfileList.value = currentList
                    } else {
                        _noFriends.value = true
                    }
                } else {
                    _noFriends.value = true
                    Log.i(TAG, "no user")
                }

            }.addOnFailureListener {
                Log.e(TAG, "no user when load all users: $it")
            }

            registration


        } else {
            otherProfileResult =
                db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                    .orderBy("user_id", Query.Direction.DESCENDING)


            val registration = otherProfileResult.get().addOnSuccessListener { querySnapshot ->

                if (querySnapshot != null) {
                    val currentList = emptyList<UserProfile>().toMutableList()
                    if (querySnapshot.documents.size != 0) {
                        for (snapshot in querySnapshot) {
                            val gender = snapshot.get("gender").toString()
                            val images = snapshot.get("images").toString()
                            val profileTime = snapshot.get("profile_time").toString()
                            val userId = snapshot.get("user_id").toString()
                            val userName = snapshot.get("user_name").toString()
                            val worriesDescription =
                                snapshot.get("worries_description").toString()
                            val worriesType = snapshot.get("worries_type").toString()
                            val age = snapshot.get("user_age").toString()
                            val selfDescription = snapshot.get("self_description").toString()

                            val newUserProfile = UserProfile(
                                userId = userId,
                                userName = userName,
                                gender = gender,
                                worryType = worriesType,
                                worriesDescription = worriesDescription,
                                imageUri = images,
                                age = age,
                                selfDescription = selfDescription
                            )
                            currentList.add(newUserProfile)
                        }
                        _otherProfileList.value = currentList
                    } else {
                        _noFriends.value = true
                    }
                } else {
                    _noFriends.value = true
                    Log.i(TAG, "no user")
                }


            }.addOnFailureListener {
                Log.e(TAG, "current friends get failed: $it")
            }
            registration
        }


    }

    fun loadWhoLikeMe(userId: String) {
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()
        Log.i(TAG, "start to load who is friend currently")
        val currentFriendList = emptyList<String>().toMutableList()
        currentFriendList.add(UserManager.userId)




        db.collection("relationship")
            .where(Filter.or(
                Filter.equalTo("sender_id", UserManager.userId),
                Filter.equalTo("current_relationship", "Like")
            ))
            .get()
            .addOnSuccessListener { snapshot ->
                Log.i(TAG, "found friend number by me: ${snapshot.documents.size}")
                if (snapshot != null && !snapshot.isEmpty) {

                    for (shot in snapshot) {
                        if (shot.data["receiver_id"].toString() == UserManager.userId) {
                            Log.i(TAG, "current receiver: ${UserManager.userId}")
                            currentFriendList.add(shot.data["sender_id"].toString())
                        } else {
                            currentFriendList.add(shot.data["receiver_id"].toString())
                        }
                        Log.i(TAG, "currentFriendList: $currentFriendList")

                    }
                    loadOtherProfile(currentFriendList)
                    Log.i(TAG, "list: ${currentFriendList}, start to find other")

                } else {
                    Log.e(TAG, "no friends")
                    loadOtherProfile(currentFriendList)
                }


            }.addOnFailureListener {
                Log.e(TAG, "cannot load friend list: $it")
            }


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
                    Log.i(TAG, "friendId is: $friendId")
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
        var currentRoomKey = friendId + UserManager.userId
        val sentTime = System.currentTimeMillis().toString()
        val data = hashMapOf(
            "key" to currentRoomKey,
            "user_a_id" to UserManager.userId,
            "user_a_name" to UserManager.userName,
            "user_b_id" to friendId,
            "user_b_name" to friendName,
            "last_sentence" to "",
            "sent_time" to sentTime,
            "send_by_id" to "",
            "user_a_img" to UserManager.userImage,
            "user_b_img" to friendImg,
            "unread_times" to 0,
            "user_a_online" to true,
            "user_b_online" to true

        )



        chatRoom.add(data).addOnSuccessListener {
            updateChatRoomKeyInRelationship(currentRoomKey, documentId)
            _matchToChatRoom.value = ChatRoom(
                id = it.id,
                key = currentRoomKey,
                selfId = UserManager.userId,
                otherId = friendId,
                selfName = UserManager.userName,
                otherName = friendName,
                lastSentence = "",
                sendById = "",
                selfImage = UserManager.userImage,
                otherImage = friendImg,
                sentTime = sentTime,
                unreadTimes = "0",
                otherOnline = true
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

            Filter.and(
                Filter.equalTo("sender_id", friendId),
                Filter.equalTo("receiver_id", UserManager.userId)
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
            Direction.Left ->
                likeOrDislike(friendId, friendName, friendImg, "Dislike", documentId)

            Direction.Right -> {
                likeOrDislike(friendId, friendName, friendImg, "Like", documentId)
                Log.i(TAG, "Liked friendId is $friendId")
            }

            else -> likeOrDislike(friendId, friendName, friendImg, "Like", documentId)
        }
    }


    fun navigateToMatch() {
        _matchToChatRoom.value = null
    }


    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "home viewModel is dead")
    }


}