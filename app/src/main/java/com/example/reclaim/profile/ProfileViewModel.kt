package com.example.reclaim.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reclaim.chatgpt.ApiClient
import com.example.reclaim.chatgpt.CompletionRequest
import com.example.reclaim.chatgpt.CompletionResponse
import com.example.reclaim.chatgpt.MessageToGPT
import com.example.reclaim.data.Images
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


data class ProfileData(
    var userName: String? = "",
    var gender: String? = "",
    var worriesDescription: String? = "",
    var worriesType: String? = "",
    var images: List<String> = emptyList()
)


enum class WorriesType {
    WORK,
    RELATIONSHIP,
    FAMILY,
    FRIENDSHIP,
    LIFE,
    HEALTH,
    SCHOOL
}

private const val TAG = "PROFILEVIEWMODEL"


class ProfileViewModel(private val databaseDao: ReclaimDatabaseDao) : ViewModel() {

    private var _userProfile = MutableLiveData<ProfileData?>()
    val userProfile: LiveData<ProfileData?>
        get() = _userProfile

    private val _messageList = MutableLiveData<MutableList<MessageToGPT>>()
    val messageList: LiveData<MutableList<MessageToGPT>>
        get() = _messageList

    private val type = WorriesType.values().map { it.toString() }
    private val prompt =
        """Force you to select one in $type for """

    val db = Firebase.firestore

    private val _readyToUploadOnFirebase = MutableLiveData<Boolean>()
    val readyToUploadOnFirebase: LiveData<Boolean>
        get() = _readyToUploadOnFirebase

    init {
        _messageList.value = mutableListOf()
        _userProfile.value = ProfileData()
        Log.i(TAG, "type is${type}")
        _readyToUploadOnFirebase.value = false

    }

    fun saveUserProfile(
        userName: String?,
        userGender: String?,
        worriesDescription: String?,
        worriesType: String?,
        images: List<String>
    ) {
        val userData = _userProfile.value
        userData?.userName = userName
        userData?.gender = userGender
        userData?.worriesDescription = worriesDescription
        userData?.worriesType = worriesType
        userData?.images = images
        _userProfile.value = userData


    }

    private fun saveUserProfileInLocal() {
        val currentUser = UserProfile(
            userId = UserManager.userId,
            userName = _userProfile.value?.userName,
            gender = _userProfile.value?.gender,
            worryType = _userProfile.value?.worriesType,
            worriesDescription = _userProfile.value?.worriesDescription,
            imageUri = _userProfile.value?.images?.get(0)
        )
        viewModelScope.launch {
            databaseDao.insertUserProfile(currentUser)
        }

    }

    private fun saveImagesInLocal() {
        _userProfile.value?.images?.forEach {
            val images = Images(userId = UserManager.userId, imageUri = it)
            viewModelScope.launch {
                databaseDao.saveImages(images)
            }

        }
    }

    private fun saveInLocalDB() {
        viewModelScope.launch {
            saveUserProfileInLocal()
            saveImagesInLocal()
        }
    }


    private fun addToChatGPT(message: String, sentBy: String, timestamp: String) {

        val currentList = _messageList.value ?: mutableListOf()
        currentList.add(MessageToGPT(message, sentBy, timestamp))
        _messageList.postValue(currentList)

        Log.i(TAG, "messageList is ${_messageList.value}")


    }

    private fun addResponse(response: String) {
        _messageList.value?.removeAt(_messageList.value?.size?.minus(1) ?: 0)
        addToChatGPT(response, MessageToGPT.SENT_BY_BOT, getCurrentTimestamp())
    }

    private fun callApi(question: String) {
        addToChatGPT("Typing...", MessageToGPT.SENT_BY_BOT, getCurrentTimestamp())

        val completionRequest = CompletionRequest(
            model = "text-davinci-003",
            prompt = question,
            max_tokens = 4000
        )

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCompletion(completionRequest)
                handleApiResponse(response)
            } catch (e: Exception) {
                addResponse("Timeout: $e")
            }
        }

    }

    private suspend fun handleApiResponse(response: Response<CompletionResponse>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                response.body()?.let { completionResponse ->
                    val result = completionResponse.choices.first()?.text

                    if (result != null) {
//                        addResponse(result)
                        val currentType = result
                        _userProfile.value?.worriesType = currentType
                        saveInLocalDB()
                        UserManager.userType = result
                        _readyToUploadOnFirebase.value = true

                        Log.i(TAG, "result is$result")
                    } else {
                        addResponse("No Choices found")
                    }
                }
            } else {
                try {
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    Log.e(
                        TAG, "GPT" +
                                jObjError.getJSONObject("error").getString("message")
                    )
                } catch (e: Exception) {
                    Log.e(
                        TAG, "GPT" +
                                e.toString()
                    )

                }
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("hh mm a", Locale.getDefault()).format(Date())
    }

    fun sendDescriptionToGPT(worriesDescription: String?) {
        val question =
            prompt + "$worriesDescription"

        addToChatGPT(question, MessageToGPT.SENT_BY_USER, getCurrentTimestamp())
        callApi(question)
    }

    fun uploadProfileToFirebase() {
        val profile = FirebaseFirestore.getInstance().collection("user_profile")

        val data = hashMapOf(
            "user_id" to UserManager.userId,
            "user_name" to _userProfile.value?.userName,
            "gender" to _userProfile.value?.gender,
            "worries_description" to _userProfile.value?.worriesDescription,
            "worries_type" to _userProfile.value?.worriesType,
            "images" to _userProfile.value?.images,
            "profile_time" to Calendar.getInstance().timeInMillis
        )

        profile.add(data)
            .addOnSuccessListener {
                Log.i(TAG, "upload success")
            }
            .addOnFailureListener {
                Log.i(TAG, "upload failed")
            }


    }


}