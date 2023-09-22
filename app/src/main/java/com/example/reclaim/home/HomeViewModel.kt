package com.example.reclaim.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HOMEVIEWMODEL"

class HomeViewModel(private val reclaimDatabaseDao: ReclaimDatabaseDao) : ViewModel() {

    private var _otherProfileList = MutableLiveData<MutableList<UserProfile>>()
    val otherProfileList: LiveData<MutableList<UserProfile>>
        get() = _otherProfileList

    val db = Firebase.firestore

    private var _firebaseDisconnect = MutableLiveData<Boolean>(false)
    val firebaseDisconnect: LiveData<Boolean>
        get() = _firebaseDisconnect

    private var _noFriends = MutableLiveData<Boolean>(false)
    val noFriends: LiveData<Boolean>
        get() = _noFriends


    init {
        Log.i(TAG, "viewModel start")
        _otherProfileList.value = emptyList<UserProfile>().toMutableList()
        loadOtherProfile()
    }

    private fun loadOtherProfile() {

            try {
                val otherResultDocument =
                    db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                        .whereEqualTo("worries_type", UserManager.userType)
                        .orderBy("user_id", Query.Direction.DESCENDING)
                        .orderBy("profile_time", Query.Direction.DESCENDING)


                otherResultDocument.addSnapshotListener { querysnapshot, e ->
                    if (e != null) {
                        _firebaseDisconnect.value = true
                        Log.i(TAG, e.toString())
                    }
                    _otherProfileList.value?.clear()


                    if (querysnapshot!!.documents.size != 0) {
                        Log.i(TAG, "querysnapshot is not empty, start to get firebase")
                        getFieldFromFirebase(querysnapshot)
                    } else {
                        loadAllProfile()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
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

    private fun loadAllProfile() {

           val otherProfileResult = db.collection("user_profile").whereNotEqualTo("user_id", UserManager.userId)
                       .orderBy("user_id", Query.Direction.DESCENDING)
                       .orderBy("profile_time", Query.Direction.DESCENDING)


           otherProfileResult.addSnapshotListener{
                   querySnapshot, e ->
               if( e != null){
                   _firebaseDisconnect.value = true
               }
               if(querySnapshot != null){
                   if(querySnapshot.documents.size != 0){
                       getFieldFromFirebase(querySnapshot)
                   }else{
                       _noFriends.value = true
                   }
               }else{
                   _noFriends.value = true
               }



       }


    }
}