package com.example.reclaim.videocall

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class RTCViewModel: ViewModel() {

    private var _showEndCallHint = MutableLiveData<Boolean>()
    val showEndCallHint: LiveData<Boolean>
        get() = _showEndCallHint

    private var _onDestroyed = MutableLiveData<Boolean>()
    val onDestroyed: LiveData<Boolean>
        get() = _onDestroyed


    init {
        _showEndCallHint.value = false
        _onDestroyed.value = false

    }

    fun listenEndCall(meetingID: String){

        val registration = FirebaseFirestore.getInstance().collection("calls").document(meetingID)
            .addSnapshotListener { value, error ->
                if (error != null){
                    Log.e(TAG, "cannot get call data: $error")
                    return@addSnapshotListener
                }
                if (value != null && value.exists()){
                    if (value?.get("type")?.toString() == "END_CALL"){
                        _showEndCallHint.value = true
                        Log.i(TAG, "found data: END, turn on logo")
                    }else{
                        Log.i(TAG, "found data: not yet finish")
                    }
                }else{
                    Log.i(TAG, "call data in empty")
                }

        }

        registration

        if (_onDestroyed.value == true){
            registration.remove()
        }


    }


    fun destroySnapshotListener(){
        _onDestroyed.value = true
    }



    companion object{
        private const val TAG = "RTCViewModel"
    }
}