package com.example.cloudspeechtotext.speechtotext


import android.util.Log

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
import java.io.InputStream

import javax.inject.Inject

class STTRequire {

    var speechClient: SpeechClient? = null

    fun recognizeResponse(byteArray: ByteArray): String{
        val response =  speechClient?.recognize(createRecognizeRequestFromVoice(byteArray))

        if (response != null) {
            for(result in response.resultsList){
                val transcript = result.getAlternatives(0).transcript
                return transcript
            }
        }

        return ""
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

    fun appendByteArrays(byteArray: ByteArray, data: ByteArray): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        try {
            outputStream.write(byteArray)
            outputStream.write(data)
        }catch (e: Exception){
            e.printStackTrace()
        }

        return outputStream.toByteArray()
    }



        fun initializeSpeechClient(credentials:InputStream) {
            try {
                val credentials =
                    GoogleCredentials.fromStream(credentials)
                val credentialsProvider = FixedCredentialsProvider.create(credentials)
                speechClient = SpeechClient.create(
                    SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
                )
            } catch (e: IOException) {
                Log.e("kya", "InitException" + e.message)
            }
        }







}