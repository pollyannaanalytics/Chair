package com.example.reclaim.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileViewModel : ViewModel() {
    private val TAG = "AlreadySignUp"

    private var _touchPeople = MutableLiveData<Int>()
    val touchPeople: LiveData<Int>
        get() = _touchPeople


    init {
        loadTouchPeople()
    }

    fun loadTouchPeople() {
        FirebaseFirestore.getInstance().collection("relationship")
            .whereEqualTo("sender_id", UserManager.userId)
            .orderBy("sender_id", Query.Direction.ASCENDING)
            .get().addOnSuccessListener {
                UserManager.touchNumber = it.size()
                _touchPeople.value = it.size()
                Log.i(TAG, "${it.size()}")
            }.addOnFailureListener {
                Log.e(TAG, "cannot load touch: $it")
            }

    }
}