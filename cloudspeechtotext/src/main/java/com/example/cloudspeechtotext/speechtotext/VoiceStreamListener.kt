package com.example.cloudspeechtotext.speechtotext

interface VoiceStreamListener {
    fun onVoiceStreaming(data: ByteArray?, size: Int)
    fun onVoiceDataAvailable(buffer: ShortArray)
}