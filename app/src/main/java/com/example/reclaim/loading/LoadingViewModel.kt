package com.example.reclaim.loading

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class LoadingViewModel: ViewModel() {

    private var _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile




    fun loadProfileToUserManager(authId: String) {
        FirebaseFirestore.getInstance().collection("user_profile")
            .whereEqualTo("user_id", authId).get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val userName = snapshot.data.get("user_name").toString()
                    val gender = snapshot.data.get("gender").toString()
                    val images = snapshot.data.get("images").toString()
                    val worriesDescription = snapshot.data.get("worries_description").toString()
                    val worriesType = snapshot.data.get("worries_type").toString()

                    val newProfile = UserProfile(
                        userId = authId,
                        userName = userName,
                        gender = gender,
                        imageUri = images,
                        worriesDescription = worriesDescription,
                        worryType = worriesType
                    )

                    _userProfile.value = newProfile
                }

            }.addOnFailureListener {
                Log.i(TAG, "loading failed: $it")
            }
    }

    fun putProfileInfoToUserManager(userProfile: UserProfile){
        UserManager.userId = userProfile.userId.toString()
        UserManager.userName = userProfile.userName.toString()
        UserManager.userImage = userProfile.imageUri.toString()
        UserManager.userType = userProfile.worryType.toString()
        UserManager.gender = userProfile.gender.toString()
        UserManager.worriesDescription = userProfile.worriesDescription.toString()
    }



    companion object{
        private const val TAG = "LoadingViewModel"
    }
}