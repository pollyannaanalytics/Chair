package com.example.reclaim.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private var _canFindProfile = MutableLiveData<Boolean>()
    val canFindProfile: LiveData<Boolean>
        get() = _canFindProfile


    private var _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile


    fun findProfileInFirebase(userId: String) {
        FirebaseFirestore.getInstance().collection("user_profile").whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    _canFindProfile.value = false
                } else {
                    for (snapshot in snapshots) {
                        val userName = snapshot.data.get("user_name").toString()
                        val gender = snapshot.data.get("gender").toString()
                        val images = snapshot.data.get("images").toString()
                        val worriesDescription = snapshot.data.get("worries_description").toString()
                        val worriesType = snapshot.data.get("worries_type").toString()
                        val age = snapshot.data.get("user_age").toString()
                        val selfDescription = snapshot.data.get("self_description").toString()

                        val userProfile = UserProfile(
                            userId = userId,
                            userName = userName,
                            gender = gender,
                            imageUri = images,
                            worriesDescription = worriesDescription,
                            worryType = worriesType,
                            age = age,
                            selfDescription = selfDescription
                        )
                        _userProfile.value = userProfile
                        _canFindProfile.value = true
                    }
                }
            }.addOnFailureListener {
                Log.e(TAG, "load failed: $it")
                _canFindProfile.value = false
            }
    }

    fun saveInUserManager(userProfile: UserProfile) {
        try {
            UserManager.userId = userProfile.userId.toString()
            UserManager.userName = userProfile.userName.toString()
            UserManager.userImage = userProfile.imageUri.toString()
            UserManager.userType = userProfile.worryType.toString()
            UserManager.gender = userProfile.gender.toString()
            UserManager.age = userProfile.age.toString()
            UserManager.worriesDescription = userProfile.worriesDescription.toString()
            UserManager.selfDescription = userProfile.selfDescription.toString()

            Log.i(TAG, "user manager: ${UserManager.selfDescription}")

        }catch (e: Exception){
            Log.e(TAG, "cannot save in usermanager: $e")
        }


    }

    companion object{
        const val TAG = "LoginViewModel"
    }
}