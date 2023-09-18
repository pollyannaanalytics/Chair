package com.example.cloudspeechtotext.speechtotext

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class VoiceStreamer  constructor(
    private val activity: Activity,
    private val context: Context
) {
    private var voiceRecorder: AudioRecord? = null
    private var voiceStreamListener: VoiceStreamListener? = null
    private var streamExecutorService: ExecutorService = Executors.newFixedThreadPool(1)
    private var isStreaming: Boolean = false

    fun registerOnVoiceListener(voiceStreamListener: VoiceStreamListener?){
        this.voiceStreamListener = voiceStreamListener
    }

    private val runnableAudioStream = Thread{
        try {
            val buffer = ShortArray(minBufferSize)
            if (voiceRecorder == null) {
                if(ActivityCompat.checkSelfPermission(context, RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    showDialogRequestRecordAudio()
                }else{
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    }
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        minBufferSize * 10
                    ).also {
                        voiceRecorder = it
                    }
                }
            }

            voiceRecorder?.apply {
                startRecording()
                while (isStreaming){
                    minBufferSize = read(buffer, 0, buffer.size)
                    voiceStreamListener?.onVoiceDataAvailable(buffer)
                    Log.i("MinBufferSize : ", "${buffer.size}")

                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    val sampleRate: Int
        get() = if(voiceRecorder != null){
            voiceRecorder!!.sampleRate
        }else 0

    fun stopVoiceStreaming() {
        isStreaming = false
        voiceRecorder?.release()
        voiceRecorder = null
        if (runnableAudioStream != null && runnableAudioStream.isAlive)
            streamExecutorService.shutdown()
    }

    private fun startVoiceStreaming() {
        isStreaming = true
        streamExecutorService.submit(runnableAudioStream)
    }

    private fun showDialogRequestRecordAudio() {
        AlertDialog.Builder(context)
            .setTitle("Record Audio Permission Required")
            .setMessage("If you want to use speech-to-text service, please agree to record audio")
            .setPositiveButton("Grant") { dialog, e ->
                dialog.dismiss()

            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onAudioRecordPermissionDenied()
            }
            .show()
    }

    private fun onAudioRecordPermissionDenied(dialogShown: Boolean = false) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, RECORD_AUDIO) && !dialogShown){
            showDialogRequestRecordAudio()
        }else{
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }

    }


    companion object {
        private const val sampleRate = 44000
        private const val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
        private const val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
        private var minBufferSize: Int = 2200
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 2


        private const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    }
}