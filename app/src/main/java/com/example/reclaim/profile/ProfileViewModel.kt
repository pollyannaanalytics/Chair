package com.example.reclaim.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val TAG = "AlreadySignUp"

    private var _friend = MutableLiveData<Int>(0)
    val friend : LiveData<Int>
        get() = _friend


    private var _touchPeople = MutableLiveData<Int>(0)
    val touchPeople: LiveData<Int>
        get() = _touchPeople


    init {
        viewModelScope.launch {
            loadTouchPeople()
            loadLiker()
        }

    }

    fun loadTouchPeople() {
        FirebaseFirestore.getInstance().collection("relationship")
            .where(
                Filter.and(
                    Filter.or(
                        Filter.equalTo("sender_id", UserManager.userId),
                        Filter.equalTo("receiver_id", UserManager.userId)
                    ),
                    Filter.equalTo("current_relationship", "Like")
                )
            ).get().addOnSuccessListener {
                UserManager.friendNumber = it.size()
                _friend.value = it.size()
            }.addOnFailureListener {
                UserManager.friendNumber = 0
                _friend.value = 0
                Log.e(TAG, "error: $it")
            }


    }

    fun loadLiker(){
        FirebaseFirestore.getInstance().collection("relationship").where(
            Filter.and(
                Filter.equalTo("receiver_id", UserManager.userId),
                Filter.notEqualTo("current_relationship", "Dislike")
            )
        ).get().addOnSuccessListener {
            UserManager.touchNumber = it.size()
            _touchPeople.value = it.size()+ _friend.value!!
        }.addOnFailureListener {
            UserManager.touchNumber = 0
            _touchPeople.value = 0
            Log.e(TAG, "load touch is fail : $it")
        }
    }




}