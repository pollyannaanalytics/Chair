package com.example.reclaim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.reclaim.databinding.ActivityMainBinding
import com.example.reclaim.databinding.ActivityRtcactivityBinding
import com.example.reclaim.videocall.Constants
import com.example.reclaim.videocall.RTCActivity
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)



        Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        binding.startMeeting.setOnClickListener {
//            if (binding.meetingId.text.toString().trim().isNullOrEmpty())
//                binding.meetingId.error = "Please enter meeting id"
//            else
                db.collection("voice_call")
                    .document(binding.meetingId.text.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                            binding.meetingId.error = "Please enter new meeting ID"
                        } else {
                            val intent = Intent(this@MainActivity, RTCActivity::class.java)
                            intent.putExtra("meetingID",binding.meetingId.text.toString())
                            intent.putExtra("isJoin",false)
                            startActivity(intent)
                        }



                    }
                    .addOnFailureListener {
                        Log.i("start_voice_call", it.toString())
                        binding.meetingId.error = "Please enter new meeting ID"
                    }
//            }
        }
        binding.joinMeeting.setOnClickListener {
            if (binding.joinMeeting.text.toString().trim().isNullOrEmpty())
                binding.joinMeeting.error = "Please enter meeting id"
            else {
                val intent = Intent(this@MainActivity, RTCActivity::class.java)
                intent.putExtra("meetingID",binding.meetingId.text.toString())
                intent.putExtra("isJoin",true)
                startActivity(intent)
            }
        }

    }
}