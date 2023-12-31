package com.example.reclaim.loading

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class LoadingViewModel: ViewModel() {

    private var _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile

    private var _haveProfileInfoInFirebase = MutableLiveData<Boolean>()
    val haveProfileInfoInFirebase: LiveData<Boolean>
        get() = _haveProfileInfoInFirebase
    private var _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome



    fun loadProfileToUserManager(authId: String) {
        try {
            FirebaseFirestore.getInstance().collection("user_profile")
                .whereEqualTo("user_id", authId).get()
                .addOnSuccessListener { snapshots ->
                    if (snapshots.documents.isEmpty() || snapshots.size() == 0){
                        _haveProfileInfoInFirebase.value = false
                        Log.i(TAG, "find no profile, should navigate to agreement page")
                    }
                    for (snapshot in snapshots) {
                        val userName = snapshot.data.get("user_name").toString()
                        val gender = snapshot.data.get("gender").toString()
                        val images = snapshot.data.get("images").toString()
                        val worriesDescription = snapshot.data.get("worries_description").toString()
                        val worriesType = snapshot.data.get("worries_type").toString()
                        val age = snapshot.data.get("user_age").toString()
                        val selfDescription = snapshot.data.get("self_description").toString()

                        val newProfile = UserProfile(
                            userId = authId,
                            userName = userName,
                            gender = gender,
                            imageUri = images,
                            worriesDescription = worriesDescription,
                            worryType = worriesType,
                            age = age,
                            selfDescription = selfDescription,
                            profileTime = 0L

                        )

                        _userProfile.value = newProfile
                        _haveProfileInfoInFirebase.value = true
                    }

                }.addOnFailureListener {
                    Log.i(TAG, "loading failed: $it")
                    _haveProfileInfoInFirebase.value = false
                }
        }catch (e: Exception){
            Log.i(TAG, "loading failed: $e")
            _haveProfileInfoInFirebase.value = false
        }

    }



    fun putProfileInfoToUserManager(){
        val userProfile = _userProfile.value
        UserManager.userId = userProfile.let { it!!.userId.toString() }
        UserManager.userName = userProfile.let { it!!.userName.toString() }
        UserManager.userImage = userProfile.let { it!!.imageUri.toString() }
        UserManager.userType = userProfile.let { it!!.worryType.toString() }
        UserManager.gender = userProfile.let { it!!.gender.toString() }
        UserManager.age = userProfile.let { it!!.age.toString() }
        UserManager.selfDescription = userProfile.let { it!!.selfDescription.toString() }

        UserManager.worriesDescription = userProfile.let { it!!.worriesDescription.toString() }
        _navigateToHome.value = true


    }



    companion object{
        private const val TAG = "LoadingViewModel"
    }
}