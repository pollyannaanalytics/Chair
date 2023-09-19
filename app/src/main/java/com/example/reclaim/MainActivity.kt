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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)




    }
}