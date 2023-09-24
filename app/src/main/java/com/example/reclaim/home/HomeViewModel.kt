package com.example.reclaim.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HOMEVIEWMODEL"

class HomeViewModel(private val reclaimDatabaseDao: ReclaimDatabaseDao) : ViewModel() {

    private var _otherProfileList = MutableLiveData<MutableList<UserProfile>>()
    val otherProfileList: LiveData<MutableList<UserProfile>>
        get() = _otherProfileList


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

    init {
        Log.i(TAG, "viewModel start")
        _onDestroyed.value = false
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()
        currentFriends = loadWhoLikeMe(UserManager.userId)
        loadOtherProfile()
    }

    private fun loadOtherProfile() {
        if (currentFriends.size != 0) {
            try {
                val otherResultDocument =
                    db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
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
                val images = listOf<String>(snapshot.data?.get("images").toString())

                val newUser = UserProfile(
                    userId = userId,
                    userName = userName,
                    gender = userGender,
                    worriesDescription = worriesDescription,
                    worryType = worriesType,
                    imageUri = images.first().removeSurrounding("[", "]").toString()
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
                db.collection("user_profile").where(
                    Filter.and(
                        Filter.notEqualTo("user_id", UserManager.userId),
                        Filter.notInArray("user_id", currentFriends)
                    )
                )
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
        val currentFriendList = emptyList<String>().toMutableList()
        val registration = db.collection("relationship")
            .where(
                Filter.or(
                    Filter.equalTo("receiver_id", UserManager.userId),
                    Filter.equalTo("sender_id", UserManager.userId)
                )
            ).whereNotEqualTo("current_relationship", "Like")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (shot in snapshot) {
                        if (shot.data.get("receiver_id") == UserManager.userId) {
                            currentFriendList.add("sender_id")
                        } else {
                            currentFriendList.add("receiver_id")
                        }
                    }
                } else {
                    Log.e("findFriends", "no friends")
                }


            }
        registration

        registration.remove()

        return currentFriendList

    }

    fun likeOrDislike(
        friendId: String,
        friendName: String,
        likeOrDislike: String,
        result: QueryDocumentSnapshot
    ) {
        Log.i("likeOrDislike", result.toString())
        result.reference.update("current_relationship", likeOrDislike)

        if (likeOrDislike == "Like") {
            Log.i(TAG, "like, to create room")
                createAChatRoom(friendId, friendName, result)
        }
    }

    private fun createAChatRoom(friendId: String, friendName: String, result: QueryDocumentSnapshot): String {
        val chatRoom = FirebaseFirestore.getInstance().collection("chat_room")

        val data = hashMapOf(
            "key" to "",
            "user_a_id" to UserManager.userId,
            "user_a_name" to UserManager.userName,
            "user_b_id" to friendId,
            "user_b_name" to friendName

        )
        var currentRoomKey = ""

        chatRoom.add(data).addOnSuccessListener {
            Log.i("TAG", "current room key is ${it.id}")
            currentRoomKey = it.id
            it.update("key", currentRoomKey)
            result.reference.update("chat_room_key", currentRoomKey)

        }.addOnFailureListener {
            Log.e(TAG, "create room failed: $it")

        }



        return currentRoomKey
    }


    fun findRelationship(friendId: String, friendName: String, direction: Direction) {
        val allRelationShip = db.collection("relationship")
            .where(
                Filter.and(
                    Filter.equalTo("sender_id", friendId),
                    Filter.equalTo("receiver_id", UserManager.userId)
                )
            )
            .orderBy("receiver_id", Query.Direction.DESCENDING)



        allRelationShip.addSnapshotListener { querySnapShot, e ->
            if (e != null) {
                return@addSnapshotListener
                Log.e("friend", "find relationship fail: $e")
            }

            if (querySnapShot != null && !querySnapShot.isEmpty) {
                Log.i("friend", "there is my friend: ${querySnapShot.documents[0].id}")
                for (query in querySnapShot) {
                    if (query.data.get("current_relationship") == "Pending") {
                        updateRelationShip(friendId, friendName, direction, query)
                    } else {
                        Log.i("relationship", "there is already relationships")
                    }
                }
            } else {
                createRelationShip(friendId, friendName, direction)
            }
        }
    }

    private fun createRelationShip(friendId: String, friendName: String, direction: Direction) {
        Log.i("friend", "there is no friend, start to create")
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
            "chat_room_key" to "null"
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
        direction: Enum<Direction>,
        result: QueryDocumentSnapshot
    ) {
        Log.i("update my friends", "friendId is $friendId")
        when (direction) {
            Direction.Left -> likeOrDislike(friendId, friendName, "Dislike", result)
            Direction.Right -> {
                likeOrDislike(friendId, friendName, "Like", result)
            }

            else -> likeOrDislike(friendId, friendName, "Like", result)
        }
    }


    fun removeProfileListener() {
        _onDestroyed.value = true
    }
}