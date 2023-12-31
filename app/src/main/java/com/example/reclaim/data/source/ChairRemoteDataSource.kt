package com.example.reclaim.data.source

import android.util.Log
import androidx.core.net.toUri
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.MessageType
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID

class ChairRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "repository"
        const val COLLECTION_CHAT_ROOM = "chat_room"
        const val ROOM_KEY = "key"
        const val COLLECTION_CHAT_RECORD = "chat_record"
        const val UNREAD_TIMES = "unread_times"
        const val IS_SEEN = "is_seen"
        const val LAST_SENTENCE = "last_sentence"
        const val SEND_BY_ID = "send_by_id"
        const val SENT_TIME = "sent_time"
        const val SEND_VIDEO_CALL_TEXT = "我開啟了視訊，一起加入吧!"
        const val MEETING_ID = "meeting_id"
        const val MEETING_OVER = "meeting_over"
        const val CONTENT = "content"
        const val CHAT_ROOM_KEY = "chat_room_key"
        const val SENDER_NAME = "sender_name"
        const val ID = "ID"
        const val MESSAGE_TYPE = "message_type"
        const val USER_A_IMG = "user_a_img"
        const val USER_B_IMG = "user_b_img"
        const val USER_A_NAME = "user_a_name"
        const val USER_B_NAME = "user_b_name"
        const val MEETING_OVER_HINT = "通話已結束"
        const val USER_PROFILE_COLLECTION = "user_profile"
        const val WORRIES_TYPE = "worryType"
        const val USER_ID = "userId"
        const val PROFILE_TIME = "profileTime"


    }

    fun getAllRecordFromRoom(chatRoomKey: String, callback: (Query, DocumentSnapshot) -> Unit) {
        db.collection(COLLECTION_CHAT_ROOM).whereEqualTo(ROOM_KEY, chatRoomKey)
            .get().addOnSuccessListener { snapshots ->

                val room = snapshots.documents.first()
                val recordRegistration = room.reference.collection(COLLECTION_CHAT_RECORD).orderBy(
                    SENT_TIME, Query.Direction.ASCENDING
                )

                callback(recordRegistration, room)
            }
    }


    fun clearUnreadCounts(documentID: String) {

        val chatRoom = db.collection(COLLECTION_CHAT_ROOM)
            .document(documentID)
        chatRoom.get().addOnSuccessListener {

            chatRoom.update(UNREAD_TIMES, 0)
        }

    }

    fun updateSeenStatus(chatRoomID: String, documentID: String) {
        Log.i(TAG, "update seen status is trigger")
        val chatRoomIsSeenOrNot =
            db.collection(COLLECTION_CHAT_ROOM).document(chatRoomID).collection(
                COLLECTION_CHAT_RECORD
            )
                .document(documentID)
        chatRoomIsSeenOrNot.update(IS_SEEN, true)

    }

    fun sendMessage(
        content: String,
        type: MessageType,
        meetingId: String = "",
        chatRoom: ChatRoom,
        chatRoomDocumentID: String,
        senderName: String,
        senderImageUri: String
    ) {
        val currentTimeString = System.currentTimeMillis().toString()


        val data = hashMapOf(
            CHAT_ROOM_KEY to chatRoom.key,
            CONTENT to content,
            SENT_TIME to currentTimeString,
            SENDER_NAME to senderName,
            MESSAGE_TYPE to type,
            IS_SEEN to false,
            MEETING_ID to meetingId,
            USER_B_IMG to chatRoom.otherImage,
            USER_A_IMG to senderImageUri,
            USER_A_NAME to senderName,
            USER_B_NAME to chatRoom.otherName,
            MEETING_OVER to false
        )

        db.collection(COLLECTION_CHAT_ROOM).document(chatRoomDocumentID).collection(
            COLLECTION_CHAT_RECORD
        )
            .add(data).addOnSuccessListener {
                updateOnChatList(content, chatRoomDocumentID, currentTimeString)
                Log.i(TAG, "add data: ${it.id}")

            }.addOnFailureListener {
                Log.e(TAG, "error: $it")
            }
    }


    fun updateOnChatList(content: String, chatRoomKey: String, currentTimeString: String) {
        var currentUnreadTime = 0
        val chatRoom = db.collection(COLLECTION_CHAT_ROOM).document(chatRoomKey)
        chatRoom.get().addOnSuccessListener {
            currentUnreadTime = it.get(UNREAD_TIMES).toString().toInt()
            currentUnreadTime++

            val updateData = mapOf<String, String>(
                ROOM_KEY to chatRoomKey,
                LAST_SENTENCE to content,
                SEND_BY_ID to UserManager.userId,
                SENT_TIME to currentTimeString,
                UNREAD_TIMES to currentUnreadTime.toString()

            )
            chatRoom.update(updateData)
            Log.i(TAG, "successfullly")
        }.addOnFailureListener {
            Log.e(TAG, "update unread times failed: $it")
        }

    }


    fun sendVideoCallMessage(
        meetingId: String,
        chatRoom: ChatRoom,
        chatRoomDocumentID: String,
        senderName: String,
        senderImageUri: String
    ) {
        val text = SEND_VIDEO_CALL_TEXT
        val type = MessageType.VIDEO_CALL
        sendMessage(text, type, meetingId, chatRoom, chatRoomDocumentID, senderName, senderImageUri)
    }


    fun stopUserJoinMeeting(chatRoomDocumentID: String, meetingId: String) {

        val chatRoom = db.collection(COLLECTION_CHAT_ROOM).document(chatRoomDocumentID)
        var chatRecordDocumentID = ""
        chatRoom.collection(COLLECTION_CHAT_RECORD).whereEqualTo(MEETING_ID, meetingId).get()
            .addOnSuccessListener { snapshot ->

                chatRecordDocumentID = snapshot.documents.firstOrNull().let { it!!.id }

                val chatRecordDocument =
                    chatRoom.collection(COLLECTION_CHAT_RECORD).document(chatRecordDocumentID)

                val dataUpdateToChatRecord = hashMapOf<String, Any>(
                    MEETING_OVER to true,
                    CONTENT to MEETING_OVER_HINT
                )

                // update end of meeting text to chat record and chat list
                chatRecordDocument.update(dataUpdateToChatRecord)
                chatRoom.update(LAST_SENTENCE, MEETING_OVER_HINT)

            }
    }


    fun uploadImageToFireStorage(stringOfUri: String) {
        var currentUri = ""
        val reference = FirebaseStorage.getInstance().reference
        val path = UUID.randomUUID().leastSignificantBits
        val imageRef = reference.child("images/$path.jpg")

        val uploadTask = imageRef.putFile(stringOfUri.toUri())

        uploadTask.addOnSuccessListener { uri ->
            imageRef.downloadUrl.addOnSuccessListener {
                currentUri = it.toString()
                UserManager.userImage = it.toString()
                Log.i(TAG, "upload successfully, url is $it")
            }


        }
        Log.i(TAG, "currentUri is $currentUri")


    }


    fun uploadUserProfile(callback: (DocumentReference)-> Unit){
        val profile = FirebaseFirestore.getInstance().collection(USER_PROFILE_COLLECTION)

        try {
            val data = hashMapOf(
                "user_id" to UserManager.userId,
                "user_name" to UserManager.userName,
                "gender" to UserManager.gender,
                "worries_description" to UserManager.worriesDescription,
                "worries_type" to UserManager.userType,
                "images" to UserManager.userImage,
                "user_age" to UserManager.age,
                "self_description" to UserManager.selfDescription,
                "profile_time" to Calendar.getInstance().timeInMillis
            )
            

            profile.add(data)
                .addOnSuccessListener {
                    callback(it)
                    Log.i(TAG, "upload success")
                }
                .addOnFailureListener {
                    Log.i(TAG, "upload failed")
                }
        } catch (e: Exception) {
            Log.e(TAG, "cannot upload: $e")
        }
    }


    fun loadOtherProfile(currentFriends: List<String>, userType: String, noFriendCallback: (Boolean) -> Boolean, otherProfileCallback: (List<UserProfile>)-> List<UserProfile>){
        if (currentFriends.isNotEmpty()){
            val otherResultDocument = db.collection(USER_PROFILE_COLLECTION)
                .whereEqualTo(WORRIES_TYPE, userType)
                .whereNotIn(USER_ID, currentFriends)
                .orderBy(USER_ID, Query.Direction.ASCENDING)
                .orderBy(PROFILE_TIME, Query.Direction.DESCENDING)

            otherResultDocument.get().addOnSuccessListener {
                    querySnapshots ->
                if (querySnapshots == null){
                    return@addOnSuccessListener
                }

                if (querySnapshots.isEmpty){
                    loadAllProfile(currentFriends, noFriendCallback, otherProfileCallback)
                    return@addOnSuccessListener
                }
                val currentList = emptyList<UserProfile>().toMutableList()

                for (querySnapshot in querySnapshots){
                    querySnapshot.reference.get().addOnSuccessListener { document ->
                        if (document == null){
                            noFriendCallback(true)
                            return@addOnSuccessListener
                        }

                        val newUserProfile = document.toObject(UserProfile::class.java)


                        currentList.add(newUserProfile.let { it!! })



                    }
                }



            }
        }else{

            val otherResultDocument = db.collection(USER_PROFILE_COLLECTION).whereNotEqualTo(
                WORRIES_TYPE, UserManager.userType)
                .orderBy(USER_ID, Query.Direction.DESCENDING)
                .orderBy(PROFILE_TIME, Query.Direction.DESCENDING)

            otherResultDocument.get().addOnSuccessListener { querySnapshots ->
                if (querySnapshots.isEmpty){
                    loadAllProfile(currentFriends, noFriendCallback, otherProfileCallback)
                    return@addOnSuccessListener
                }

                val currentList = emptyList<UserProfile>().toMutableList()

                for (querySnapshot in querySnapshots){
                    val newUserProfile = querySnapshot.toObject<UserProfile>()

                    currentList.add(newUserProfile.let { it!! })
                }
                otherProfileCallback(currentList)

            }
        }

    }


    private fun loadAllProfile(currentFriends: List<String>, noFriendCallback: (Boolean) -> Boolean, otherProfileList: (List<UserProfile>)-> List<UserProfile>){
        var otherProfileResult: Query? = null

        if (currentFriends.isNotEmpty()){
            otherProfileResult = db.collection(USER_PROFILE_COLLECTION).whereNotIn(USER_ID, currentFriends)
                .orderBy(USER_ID, Query.Direction.DESCENDING)
                .orderBy(PROFILE_TIME, Query.Direction.DESCENDING)

            otherProfileResult.get().addOnSuccessListener { querySnapshots ->
                val currentList = emptyList<UserProfile>().toMutableList()

                if (querySnapshots == null || querySnapshots.isEmpty){
                    noFriendCallback(true)
                    return@addOnSuccessListener
                }
                for (querySnapshot in querySnapshots){
                    querySnapshot.reference.get().addOnSuccessListener { documentSnapshot ->

                        val userProfile = documentSnapshot.toObject<UserProfile>()
                        currentList.add(userProfile.let { it!! })
                    }
                    otherProfileList(currentList)
                }


            }
        }else{
            otherProfileResult = db.collection(USER_PROFILE_COLLECTION).whereEqualTo(USER_ID, UserManager.userId).orderBy(UserManager.userId, Query.Direction.DESCENDING)
            otherProfileResult.get().addOnSuccessListener { querySnapshots ->
                if (querySnapshots == null || querySnapshots.isEmpty){
                    return@addOnSuccessListener
                }
                val currentList = emptyList<UserProfile>().toMutableList()
                for (snapshot in querySnapshots){
                    val userProfile = snapshot.toObject<UserProfile>()
                    currentList.add(userProfile)
                }
                otherProfileList(currentList)


            }
        }
    }


    fun loadWhoLikeMe(userID: String){

    }






}