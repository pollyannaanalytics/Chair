package com.example.cloudspeechtotext.speechtotext

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.cloudspeechtotext.R
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.RecognizeRequest
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings
import com.google.protobuf.ByteString
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {


    private lateinit var mVoiceRecorder: VoiceRecorder
    private var speechClient: SpeechClient ?= null

    private var byteArray: ByteArray? = null

    private var recordingThread: Thread? = null

    private fun transcribeRecording(byteArray: ByteArray?) {

        try {
            Log.e("API_CALL", "API CALL STARTED...")
            recordingThread = Thread(){
                @Override
                fun run(){
                    val response = speechClient?.recognize(byteArray?.let {
                        createRecognizeRequestFromVoice(
                            it
                        )
                    })
                    if (response != null) {
                        for(result in response.resultsList){
                            val transcript = result.getAlternatives(0).transcript
                            updateResult(transcript)
                        }
                    }
                }
            }
        }catch (e: Exception){
            Log.e("API_CALL", "$e")
        }

    }

    private fun createRecognizeRequestFromVoice(audioData: ByteArray): RecognizeRequest {
        val audioBytes = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(audioData)).build()
        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setSampleRateHertz(16000)
            .setLanguageCode("en-US")
            .build()

        return RecognizeRequest.newBuilder().setConfig(config).setAudio(audioBytes).build()
    }

    private fun appendByteArrays(byteArray: ByteArray, data: ByteArray): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        try {
            outputStream.write(byteArray)
            outputStream.write(data)
        }catch (e: Exception){
            e.printStackTrace()
        }

        return outputStream.toByteArray()
    }

    private fun updateResult(transcript: String) {}

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }


    private val mVoiceCallback = object : VoiceRecorder.Callback {
        override fun onVoiceStart() {
            super.onVoiceStart()
        }

        override fun onVoice(data: ByteArray, size: Int) {
            super.onVoice(data, size)
            byteArray = byteArray?.let { appendByteArrays(it, data) }

        }

        override fun onVoiceEnd() {
            super.onVoiceEnd()
            Log.e("kya", "" + byteArray)
            transcribeRecording(byteArray)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    fun initializeSpeechClient() {
        try {
            val credentials =
                GoogleCredentials.fromStream(resources.openRawResource(R.raw.credentials))
            val credentialsProvider = FixedCredentialsProvider.create(credentials)
            speechClient = SpeechClient.create(
                SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
            )
        } catch (e: IOException) {
            Log.e("kya", "InitException" + e.message)
        }
    }


    }

    private fun stopVoiceRecorder() {
        TODO("Not yet implemented")
    }

    private fun startVoiceRecorder() {
        if(mVoiceRecorder != null){
            mVoiceRecorder.stop()
        }

//        mVoiceRecorder = VoiceRecorder(mVoi)
    }
}