package com.example.reclaim.videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import com.example.reclaim.MainActivity

import com.example.reclaim.R
import com.example.reclaim.databinding.ActivityMainBinding
import com.example.reclaim.databinding.ActivityRtcactivityBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver


// declare this is experimental in coroutine API
@ExperimentalCoroutinesApi
class RTCActivity : AppCompatActivity() {

    private val audioManager by lazy { RTCAudioManager.create(this) }
    private lateinit var rtcClient: RTCClient

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }


    val TAG = "MainActivity"

    private var meetingID: String = "test-call"

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)


        if (intent.hasExtra("meetingID")) {
            meetingID = intent.getStringExtra("meetingID")!!
        }
        if (intent.hasExtra("isJoin")) {
            isJoin = intent.getBooleanExtra("isJson", false)
        }

        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)


        binding.switchCameraButton.setOnClickListener {
           rtcClient.switchCamera()
        }


        binding.audioOutputButton.setOnClickListener {
            if(inSpeakerMode){
                inSpeakerMode = false
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
        }


        binding.videoButton.setOnClickListener {
            if(isVideoPaused){
                isVideoPaused = false
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)

            }else{
                isVideoPaused = true
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient.enableVideo(isVideoPaused)
        }

        binding.micButton.setOnClickListener {
            if(isMute){
                isMute = false
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            }else{
                isMute = true
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient.enableAudio(isMute)
        }

        binding.endCallButton.setOnClickListener {
            rtcClient.endCall(meetingID)
            binding.remoteView.isGone = false
            Constants.isCallEnded = true
            finish()
            startActivity(Intent(this@RTCActivity, MainActivity::class.java))

        }
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                AUDIO_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
           requestCameraAndAudioPermission()
        }
    }

    private fun onCameraAndAudioPermissionGranted(){
        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver(){
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    SignalingClient.sendIceCandidate(p0, isJoin)
                    rtcClient.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.e(TAG, "onAddStream: $p0")
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onIceConnectionChange: $p0")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.e(TAG, "onIceConnectionReceivingChange: $p0")
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    Log.e(TAG, "onConnectionChange: $newState")
                }

                override fun onDataChannel(p0: DataChannel?) {
                    Log.e(TAG, "onDataChannel: $p0")
                }

                override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onStandardizedIceConnectionChange: $newState")
                }

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                    Log.e(TAG, "onAddTrack: $p0 \n $p1")
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    Log.e(TAG, "onTrack: $transceiver" )
                }
            }
        )
    }

    private fun requestCameraAndAudioPermission() {
        TODO("Not yet implemented")
    }

}