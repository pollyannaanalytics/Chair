package com.example.reclaim.videocall

import android.Manifest
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.reclaim.R
import com.example.reclaim.databinding.ActivityRtcactivityBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi



// declare this is experimental in coroutine API
@ExperimentalCoroutinesApi
class RTCActivity : AppCompatActivity() {

    private val audioManager by lazy { RTCAudioManager.create(this) }
    companion object{
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }



    val TAG = "MainActivity"

    private var meetingID : String = "test-call"

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (intent.hasExtra("meetingID")){
            meetingID = intent.getStringExtra("meetingID")!!
        }
        if(intent.hasExtra("isJoin")){
            isJoin = intent.getBooleanExtra("isJson", false)
        }




//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_rtcactivity)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
    }

}