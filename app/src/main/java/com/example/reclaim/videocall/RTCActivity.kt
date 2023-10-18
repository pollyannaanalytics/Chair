package com.example.reclaim.videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@ExperimentalCoroutinesApi
class RTCActivity : AppCompatActivity() {

    private val viewModel: RTCViewModel by lazy {
        ViewModelProvider(this).get(RTCViewModel::class.java)
    }


    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private lateinit var rtcClient: RTCClient
    private var signallingClient: SignalingClient? = null

    private val audioManager by lazy { RTCAudioManager.create(this) }

    val TAG = "activityconnected"

    private lateinit var switchCameraButton: ImageView
    private lateinit var audioOutputButton: ImageView
    private lateinit var videoButton: ImageView
    private lateinit var micButton: ImageView
    private lateinit var endCallButton: ImageView

    private var meetingID: String = "test-call"

    private var onDestroyed = false

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtcactivity)

        switchCameraButton = findViewById(R.id.switch_camera_button)
        audioOutputButton = findViewById(R.id.audio_output_button)
        videoButton = findViewById(R.id.video_button)
        micButton = findViewById(R.id.mic_button)
        endCallButton = findViewById(R.id.end_call_button)

        viewModel.listenEndCall(meetingID)

        if (intent.hasExtra("meetingID"))
            meetingID = intent.getStringExtra("meetingID")!!
        if (intent.hasExtra("isJoin"))
            isJoin = intent.getBooleanExtra("isJoin", false)

        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        switchCameraButton.setOnClickListener {
            rtcClient.switchCamera()
        }
        audioOutputButton.setOnClickListener {
            if (inSpeakerMode) {
                inSpeakerMode = false
                audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                inSpeakerMode = true
                audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
        }

        viewModel.showEndCallHint.observe(this){
            if (it == true){
                findViewById<TextView>(R.id.end_call_hint).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.end_call).visibility = View.VISIBLE
            }else{
                findViewById<TextView>(R.id.end_call_hint).visibility = View.GONE
                findViewById<ImageView>(R.id.end_call).visibility = View.GONE
            }
        }




        videoButton.setOnClickListener {
            if (isVideoPaused) {
                isVideoPaused = false
                videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            } else {
                isVideoPaused = true
                videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient.enableVideo(isVideoPaused)
        }
        micButton.setOnClickListener {
            if (isMute) {
                isMute = false
                micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                isMute = true
                micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient.enableAudio(isMute)
        }



        endCallButton.setOnClickListener {
            rtcClient.endCall(meetingID)
            findViewById<SurfaceViewRenderer>(R.id.remote_view).isGone = false
            Constants.isCallEnded = true
            finish()
            startActivity(Intent(this@RTCActivity, MainActivity::class.java))
        }
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, AUDIO_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted() {

        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)

                    signallingClient?.let {
                        it.sendIceCandidate(p0, isJoin)
                        rtcClient.addIceCandidate(p0)
                    }


                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.e(TAG, "onAddStream: ${p0?.id}")
                    p0?.videoTracks?.get(0)
                        ?.addSink(findViewById<SurfaceViewRenderer>(R.id.remote_view))
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
                    Log.e(TAG, "onAddTrack: ${p0?.track()} \n ${p1?.size}")
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    Log.e(TAG, "onTrack: $transceiver")
                }
            }
        )

        rtcClient.initSurfaceView(findViewById(R.id.remote_view))
        rtcClient.initSurfaceView(findViewById<SurfaceViewRenderer>(R.id.local_view))
        rtcClient.startLocalVideoCapture(findViewById<SurfaceViewRenderer>(R.id.local_view))
        signallingClient = SignalingClient(meetingID, createSignallingClientListener())
        if (!isJoin)
            rtcClient.call(sdpObserver, meetingID)
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            endCallButton.isClickable = true
            Log.i(TAG, "onConnection Established")
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            rtcClient.answer(sdpObserver, meetingID)
            findViewById<ProgressBar>(R.id.remote_view_loading).isGone = true
            Log.i(TAG, "onOfferReceived")
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            findViewById<ProgressBar>(R.id.remote_view_loading).isGone = true
            Log.i(TAG, "onAnswerReceived")
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
            Log.i(TAG, "onIceCandidateReceived")
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(meetingID)
                finish()

                Log.i(TAG, "onIceCandidateReceived")
                startActivity(Intent(this@RTCActivity, MainActivity::class.java))
            }
        }
    }




    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
            !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION),
                CAMERA_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera And Audio Permission Required")
            .setMessage("This app need the camera and audio to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAndAudioPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        signallingClient?.destroy()
        viewModel.destroySnapshotListener()

        onDestroyed = true
        super.onDestroy()
    }
}