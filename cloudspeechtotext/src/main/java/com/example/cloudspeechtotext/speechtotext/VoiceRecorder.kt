package com.example.cloudspeechtotext.speechtotext

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

class VoiceRecorder {

    companion object{
        private val SAMPLE_RATE_CANDIDATES = arrayOf(16000, 11025, 22050, 44100)
        private val CANNEL = AudioFormat.CHANNEL_IN_MONO
        private val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val AMPLITUDE_THRESHOLD = 1500
        private const val SPEECH_TIMEOUT_MILLIS = 2000
        private const val MAX_SPEECH_LENGTH_MILLIS = 30 * 1000
    }


   interface Callback{
        fun onVoiceStart(){}

        fun onVoice(data: ByteArray, size: Int){

        }

        fun onVoiceEnd(){

        }
    }

    private var mCallback: Callback? = null

    private var mAudioRecord: AudioRecord ? = null

    private var mThread: Thread ?= null

    private var mBuffer: ByteArray? = null

    private val mLock = Any()

    /** The timestamp of the last time that voice is heard.  */
    private var mLastVoiceHeardMillis = Long.MAX_VALUE

    /** The timestamp when the current voice is started.  */
    private var mVoiceStartedMillis: Long = 0

    fun VoiceRecorder(callback: Callback) {
        mCallback = callback
    }


    fun start(){

        // stop recording if is currently ongoing
        stop()
        mAudioRecord = createAudioRecord()

        if(mAudioRecord == null){
            throw IllegalArgumentException("Cannot instantiate VoiceRecorder")
        }


        // start to record
        if(mAudioRecord != null){
            mAudioRecord!!.startRecording()
        }


        // start processing the captured audio

        mThread = Thread(ProcessVoice(this))

        if(mThread != null){
            mThread!!.start()
        }
    }


    private fun createAudioRecord(): AudioRecord? {
        for(sampleRate in SAMPLE_RATE_CANDIDATES){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate, CANNEL, ENCODING)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE){
                continue
            }
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, CANNEL, ENCODING, sizeInBytes)

            if(audioRecord.sampleRate == AudioRecord.STATE_INITIALIZED){
                mBuffer = ByteArray(sizeInBytes)
                return audioRecord
            }else{
                audioRecord.release()
            }
        }

        return null
    }

    @OptIn(InternalCoroutinesApi::class)
   fun stop() {
        synchronized(mLock){
            dismiss()
            if(mThread != null){
                mThread!!.interrupt()
                mThread = null
            }

            if(mAudioRecord != null){
                mAudioRecord!!.stop()
                mAudioRecord!!.release()
                mAudioRecord = null
            }

            mBuffer = null
        }
    }

    private fun dismiss() {
        if(mLastVoiceHeardMillis != Long.MAX_VALUE){
            mLastVoiceHeardMillis = Long.MAX_VALUE

            if (mCallback != null){
                mCallback!!.onVoiceEnd()
            }

        }
    }

    private class ProcessVoice(val recorder: VoiceRecorder) : Runnable {

        @OptIn(InternalCoroutinesApi::class)
        override fun run() {
            while (true) {
                synchronized(recorder.mLock) {
                    if (Thread.currentThread().isInterrupted) {
                        return
                    }
                    val size: Int? =
                        recorder.mBuffer?.let { recorder.mAudioRecord?.read(it, 0, recorder.mBuffer!!.size)
                            ?: 0 }
                    val now = System.currentTimeMillis()
                    if (recorder.mBuffer != null){
                        if (size?.let { isHearingVoice(recorder.mBuffer!!, it) } == true) {
                            if (recorder.mLastVoiceHeardMillis == Long.MAX_VALUE) {
                                recorder.mVoiceStartedMillis = now
                                recorder.mCallback?.onVoiceStart()
                            }
                            recorder.mCallback?.onVoice(recorder.mBuffer!!, size)
                            recorder.mLastVoiceHeardMillis = now
                            if (now - recorder. mVoiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
                                end()
                            }
                        } else if (recorder.mLastVoiceHeardMillis != Long.MAX_VALUE) {
                            if (size != null) {
                                recorder.mCallback?.onVoice(recorder.mBuffer!!, size)
                            }
                            if (now - recorder.mLastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS) {
                                end()
                            }
                        }
                    }

                }
            }
        }

        private fun end() {
            recorder.mLastVoiceHeardMillis = Long.MAX_VALUE
            recorder.mCallback?.onVoiceEnd()
        }

        private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
            var i = 0
            while (i < size - 1) {

                // The buffer has LINEAR16 in little endian.
                var s = buffer[i + 1].toInt()
                if (s < 0) s *= -1
                s = s shl 8
                s += Math.abs(buffer[i].toInt())
                if (s > AMPLITUDE_THRESHOLD) {
                    return true
                }
                i += 2
            }
            return false
        }
    }


}


