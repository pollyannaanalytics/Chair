package com.example.reclaim.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
private const val TAG = "WorriesViewModel"
class WorriesInputViewModel: ViewModel() {


   fun uploadUserProfile() {
        val profile = FirebaseFirestore.getInstance().collection("user_profile")
        try {
            val data = hashMapOf(
                "user_id" to UserManager.userId,
                "user_name" to UserManager.userName,
                "gender" to UserManager.gender,
                "worries_description" to UserManager.worriesDescription,
                "worries_type" to UserManager.userType,
                "images" to UserManager.userImage,
                "profile_time" to Calendar.getInstance().timeInMillis
            )
            Log.i(TAG, "my profile is $data")

            profile.add(data)
                .addOnSuccessListener {
                    Log.i(TAG, "upload success")
                }
                .addOnFailureListener {
                    Log.i(TAG, "upload failed")
                }
        }catch (e: Exception){
            Log.e(TAG, "cannot upload: $e")
        }
    }
}