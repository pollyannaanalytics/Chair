package com.example.cloudspeechtotext.speechtotext

import android.content.Context
import android.net.Credentials
import com.example.cloudspeechtotext.R

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.InputStream

class SpeechCredentialsProvider(private val context: Context) : CredentialsProvider {

    override fun getCredentials(): com.google.auth.Credentials? {
        val fileStream: InputStream = context.resources.openRawResource(R.raw.credentials)
        return ServiceAccountCredentials.fromStream(fileStream)
    }
}