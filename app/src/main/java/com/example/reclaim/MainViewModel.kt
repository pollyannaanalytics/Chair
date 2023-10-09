package com.example.reclaim

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.UserManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel: ViewModel() {

    // use map to control all fragments' visibility
    private val _elementVisibleMap = MutableLiveData<Map<String, Boolean>>()
    val elementVisibleMap: LiveData<Map<String, Boolean>>
        get() = _elementVisibleMap


    // use Boolean to control whole tool bar
    private val _showWholeToolBar = MutableLiveData<Boolean>(true)
    val showWholeToolbar: LiveData<Boolean>
        get() = _showWholeToolBar

    init {
        _elementVisibleMap.value = mapOf(
            "homePage" to false,
            "profilePage" to false,
            "chatList" to false,
        )
    }


    // determine hide view or not depend on fragment id
    private fun setElementVisibility(fragmentId: String, visible: Boolean) {
        val currentMap = _elementVisibleMap.value.orEmpty().toMutableMap()

        // Set all elements to false initially
        for (key in _elementVisibleMap.value?.keys.orEmpty()) {
            currentMap[key] = false
        }
        currentMap[fragmentId] = visible

        _elementVisibleMap.value = currentMap
    }

    fun setToolBar(show: Boolean) {
        _showWholeToolBar.value = show
    }


    fun showElementOrToolBar(fragmentId: String, showElement: Boolean, showToolbar: Boolean){
        setElementVisibility(fragmentId, showElement)
        setToolBar(showToolbar)

    }

    fun updateOnline(onlineOrNot: Boolean){
        FirebaseFirestore.getInstance().collection("chat_room").where(
            Filter.or(
                Filter.equalTo("user_a_id", com.example.reclaim.data.UserManager.userId),
                Filter.equalTo("user_b_id", com.example.reclaim.data.UserManager.userId)
            )
        ).get().addOnSuccessListener { snapshots ->

            for (snapshot in snapshots){
                val userAId = snapshot.get("user_a_id").toString()
                val userBId = snapshot.get("user_b_id").toString()
                if (userAId == com.example.reclaim.data.UserManager.userId){
                    FirebaseFirestore.getInstance().collection("chat_room").document(snapshot.id)
                        .update("user_a_online", onlineOrNot)
                }else{
                    FirebaseFirestore.getInstance().collection("chat_room").document(snapshot.id)
                        .update("user_b_online", onlineOrNot)
                }

            }


        }.addOnFailureListener {
            Log.e(TAG, "cannot update online situation: $it" )

        }
    }
companion object{
    const val TAG = "MainViewModel"
}




}