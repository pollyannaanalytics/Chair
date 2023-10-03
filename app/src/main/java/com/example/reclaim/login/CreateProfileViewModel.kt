package com.example.reclaim.login

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.Question
import com.example.reclaim.data.UserManager
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateProfileViewModel: ViewModel() {
private val TAG = "createprofile"


    fun uploadImageToFireStorage(stringOfUri: String) {
        var currentUri = ""
        val reference = FirebaseStorage.getInstance().reference
        val path = UUID.randomUUID().leastSignificantBits
        val imageRef = reference.child("images/$path.jpg")

        val uploadTask = imageRef.putFile(stringOfUri.toUri())

        uploadTask.addOnSuccessListener { uri ->
            imageRef.downloadUrl.addOnSuccessListener {
                currentUri = it.toString()
                UserManager.userImage = currentUri
                Log.i(TAG, "upload successfully, url is $it")
            }


        }.addOnFailureListener {
            Log.e(TAG, "failed: $it")
        }
        Log.i(TAG, "currentUri is $currentUri")


    }
}