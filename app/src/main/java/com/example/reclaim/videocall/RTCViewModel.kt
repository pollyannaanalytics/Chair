package com.example.reclaim.videocall

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.R
import com.google.firebase.firestore.FirebaseFirestore

class RTCViewModel: ViewModel() {

    private var _showEndCallHint = MutableLiveData<Boolean>()
    val showEndCallHint: LiveData<Boolean>
        get() = _showEndCallHint

    init {

    }

    fun endCallShowHint(meetingID: String){

        val registration = FirebaseFirestore.getInstance().collection("calls").document(meetingID).addSnapshotListener { value, error ->
            if (value?.get("type")?.toString() == "END_CALL"){
                findViewById<ImageView>(R.id.end_call).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.end_call_hint).visibility = View.VISIBLE
            }else{
                findViewById<ImageView>(R.id.end_call).visibility = View.GONE
                findViewById<ImageView>(R.id.end_call_hint).visibility = View.GONE
            }
        }
        registration
        if (!onDestroyed){
            registration.remove()
        }

    }
}