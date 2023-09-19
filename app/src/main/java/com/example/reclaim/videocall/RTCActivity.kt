package com.example.reclaim.videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.reclaim.MainActivity
import com.example.reclaim.R

import com.example.reclaim.databinding.ActivityRtcactivityBinding

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink


// declare this is experimental in coroutine API
@ExperimentalCoroutinesApi
class RTCActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityRtcactivityBinding.inflate(layoutInflater)
    }
    private val audioManager by lazy { RTCAudioManager.create(this) }
    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient

//    private var mVoiceRecorder: VoiceRecorder? = null

    private var byteArray: ByteArray? = null

//    private val fromSpeechToText = STTRequire()

    private var mediaPlayer: MediaPlayer? = null

    private var transcript: String? = null


//
//    private val mVoiceCallback = object : com.example.speechtotext.speechtotext.VoiceRecorder.Callback {
//        override fun onVoiceStart() {
//            super.onVoiceStart()
//        }
//
//        override fun onVoice(data: ByteArray, size: Int) {
//            super.onVoice(data, size)
//            byteArray = byteArray?.let { appendByteArrays(it, data) }
//
//        }
//
//        override fun onVoiceEnd() {
//            super.onVoiceEnd()
//            Log.e("kya", "" + byteArray)
//            transcribeRecording(byteArray)
//        }
//    }
//
//    private fun transcribeRecording(byteArray: ByteArray?) {
//
//        try {
//            Log.e("API_CALL", "API CALL STARTED...")
//            recordingThread = Thread(){
//                @Override
//                fun run(){
//                    val response = speechClient?.recognize(byteArray?.let {
//                        createRecognizeRequestFromVoice(
//                            it
//                        )
//                    })
//                    if (response != null) {
//                        for(result in response.resultsList){
//                            val transcript = result.getAlternatives(0).transcript
//                            updateResult(transcript)
//                        }
//                    }
//                }
//            }
//        }catch (e: Exception){
//            Log.e("API_CALL", "$e")
//        }
//
//    }

//    private fun createRecognizeRequestFromVoice(audioData: ByteArray): RecognizeRequest{
//        val audioBytes = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(audioData)).build()
//        val config = RecognitionConfig.newBuilder()
//            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//            .setSampleRateHertz(16000)
//            .setLanguageCode("en-US")
//            .build()
//
//        return RecognizeRequest.newBuilder().setConfig(config).setAudio(audioBytes).build()
//    }
//
//    private fun appendByteArrays(byteArray: ByteArray, data: ByteArray): ByteArray? {
//        val outputStream = ByteArrayOutputStream()
//        try {
//            outputStream.write(byteArray)
//            outputStream.write(data)
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//
//        return outputStream.toByteArray()
//    }
//
//    private fun updateResult(transcript: String) {}

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }


    val TAG = "RTCActivity"

    private var meetingID: String = "test-call"

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true

    private var permissionToRecordAccepted = false

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        if (intent.hasExtra("meetingID")) {
            meetingID = intent.getStringExtra("meetingID")!!
        }
        if (intent.hasExtra("isJoin")) {
            isJoin = intent.getBooleanExtra("isJoin", false)

        }

        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)


        binding.switchCameraButton.setOnClickListener {
            rtcClient.switchCamera()
        }


        binding.audioOutputButton.setOnClickListener {
            if (inSpeakerMode) {
                inSpeakerMode = false
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
        }


        binding.videoButton.setOnClickListener {
            if (isVideoPaused) {
                isVideoPaused = false
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)

            } else {
                isVideoPaused = true
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient.enableVideo(isVideoPaused)
        }

        binding.micButton.setOnClickListener {
            if (isMute) {
                isMute = false
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
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

//        binding.startSttBtn.setOnClickListener {
//
////            if(permissionToRecordAccepted){
////                startVoiceRecorder()
////            }else{
////                stopVoiceRecorder()
////            }
//            startVoiceRecorder()
//            fromSpeechToText.initializeSpeechClient(resources.openRawResource(com.example.cloudspeechtotext.R.raw.credentials))
//        }
//
//        binding.stopSttBtn.setOnClickListener {
////            stopVoiceRecorder()
////            byteArray?.let { clearByteArray(it) }
//
//        }


    }


//    private fun initializeSpeechClient() {
//        try {
//            val credentials =
//                GoogleCredentials.fromStream(resources.openRawResource(R.raw.credentials))
//            val credentialsProvider = FixedCredentialsProvider.create(credentials)
//            speechClient = SpeechClient.create(
//                SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
//            )
//        } catch (e: IOException) {
//            Log.e("kya", "InitException" + e.message)
//        }
//    }

//    private fun stopVoiceRecorder() {
//        TODO("Not yet implemented")
//    }
//
//    private fun startVoiceRecorder() {
//        if(mVoiceRecorder != null){
//            mVoiceRecorder.stop()
//        }

////        mVoiceRecorder = VoiceRecorder(mVoi)
//    }

    val remoteView: SurfaceViewRenderer
        get() = binding.remoteView
    val localView: SurfaceViewRenderer
        get() = binding.localView

    private fun onCameraAndAudioPermissionGranted() {
        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {

                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    if (p0 != null && p0.serverUrl.isNullOrBlank()) {
                        Log.e(TAG, "IceCandidate serverUrl is empty or null")
                    } else {

                        Log.i(TAG, "IceCandidate serverUrl: ${p0?.serverUrl}")
                        signallingClient.sendIceCandidate(p0, isJoin)
                        rtcClient.addIceCandidate(p0)
                    }


                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)


                    p0?.videoTracks?.get(0)?.addSink(remoteView as VideoSink)
                    Log.e(TAG, "HereonAddStream: ${p0?.videoTracks?.get(0)?.id()?.length} ")
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
                    Log.e(TAG, "onTrack: $transceiver")
                }
            }
        )
        rtcClient.initSurfaceView(remoteView as SurfaceViewRenderer)
        rtcClient.initSurfaceView(localView as SurfaceViewRenderer)
        rtcClient.startLocalVideoCapture(localView as SurfaceViewRenderer)
        signallingClient = SignalingClient(meetingID, createSignallingClientListener())
        if (!isJoin)
            rtcClient.call(sdpObserver, meetingID)
    }


    val endCallBtn: ImageView
        get() = binding.endCallButton
    val remoteViewLoading: ProgressBar
        get() = binding.remoteViewLoading

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            endCallBtn.isClickable = true
            Log.i("anwserreceive", "connect")

        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            rtcClient.answer(sdpObserver, meetingID)
            remoteView.isGone = true
            Log.i("anwserreceive", "offer")
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            remoteViewLoading.isGone = true
            Log.i("anwserreceive", "answer")
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
            Log.i("anwserreceive", "addCandidate")
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(meetingID)
                finish()
                Log.i("anwserreceive", "end")
                startActivity(Intent(this@RTCActivity, MainActivity::class.java))
            }
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
            .setPositiveButton("Grant") { dialog, e ->
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

//    private fun transcribeRecording(byteArray: ByteArray?) {
//
//        try {
//            lifecycleScope.launch {
//                Log.e("API_CALL", "API CALL STARTED...")
//                if (byteArray != null) {
//                    transcript = async { fromSpeechToText.recognizeResponse(byteArray) }.await()
//
//                    if (transcript != null && transcript != "") {
//                        updateResult(transcript!!)
//                    }
//
//                }
//
//            }
//        } catch (e: Exception) {
//            Log.e("API_CALL", "$e")
//        }
//
//    }

//    private val mVoiceCallback = object : VoiceRecorder.Callback {
//        override fun onVoiceStart() {
//            super.onVoiceStart()
//        }
//
//        override fun onVoice(data: ByteArray, size: Int) {
//            super.onVoice(data, size)
//            byteArray = byteArray?.let { fromSpeechToText.appendByteArrays(it, data) }
//
//        }
//
//        override fun onVoiceEnd() {
//            super.onVoiceEnd()
//            Log.e("kya", "" + byteArray)
//            transcribeRecording(byteArray)
//        }
//    }
//
//    private fun stopVoiceRecorder() {
//        if (mVoiceRecorder != null) {
//            mVoiceRecorder!!.stop()
//
//            mVoiceRecorder = null
//        }
//
//        if (recordingThread != null) {
//            try {
//                recordingThread!!.join()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            recordingThread = null
//        }
//    }
//
//    private fun startVoiceRecorder() {
//        if (mVoiceRecorder != null) {
//            mVoiceRecorder!!.stop()
//        }
//
//        mVoiceRecorder = VoiceRecorder(mVoiceCallback)
//    }

//    private fun clearByteArray(array: ByteArray) {
//        array.fill(0, 0, array.size - 1)
//    }
//
//    private fun playSound() {
//        mediaPlayer = MediaPlayer.create(this, com.example.cloudspeechtotext.R.raw.credentials)
//        if (mediaPlayer != null) {
//
//            mediaPlayer!!.setOnCompletionListener { mediaPlayer!!.release() }
//            mediaPlayer!!.start()
//        }
//
//    }
//
//    private fun updateResult(transcript: String) {
//        lifecycleScope.launch {
//            playSound()
//            if (byteArray != null) {
//                clearByteArray(byteArray!!)
//            }
////            stopVoiceRecorder()
//        }
//    }


    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

//    override fun onDestroy() {
//        signallingClient.destroy()
//        super.onDestroy()
//    }
}
