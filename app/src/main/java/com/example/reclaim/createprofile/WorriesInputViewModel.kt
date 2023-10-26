package com.example.reclaim.createprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reclaim.chatgpt.ApiClient
import com.example.reclaim.chatgpt.CompletionRequest
import com.example.reclaim.chatgpt.CompletionResponse
import com.example.reclaim.chatgpt.MessageToGPT
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.WorriesType
import com.example.reclaim.data.source.ChairRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WorriesInputViewModel(
    private val chairRepository: ChairRepository
) : ViewModel() {

    companion object {
        private const val TAG = "WorriesViewModel"
        private const val COMPLETION_MODEL = "text-davinci-003"
        private const val MAX_TOKEN = 1000
        private const val TYPING = "Typing..."
    }

    private var _showLottie = MutableLiveData<Boolean>()
    val showLottie: LiveData<Boolean>
        get() = _showLottie
    private val _messageList = MutableLiveData<MutableList<MessageToGPT>>()
    val messageList: LiveData<MutableList<MessageToGPT>>
        get() = _messageList


    private val type = WorriesType.values().map { it.toString() }
    private val prompt =
        """Force you to select one in $type for """

    val db = Firebase.firestore

    init {
        _messageList.value = mutableListOf()


    }

    private fun addToChatGPT(message: String, sentBy: String, timeString: String) {

        val currentList = _messageList.value ?: mutableListOf()
        currentList.add(MessageToGPT(message, sentBy, timeString))
        _messageList.postValue(currentList)

        Log.i(TAG, "messageList is ${_messageList.value}")


    }

    private fun addResponse(response: String) {
        _messageList.value?.removeAt(_messageList.value?.size?.minus(1) ?: 0)
        addToChatGPT(response, MessageToGPT.SENT_BY_BOT, getCurrentTimestamp())
    }

    private fun callApi(question: String) {
        addToChatGPT(TYPING, MessageToGPT.SENT_BY_BOT, getCurrentTimestamp())

        val completionRequest = CompletionRequest(
            model = COMPLETION_MODEL,
            prompt = question,
            max_tokens = MAX_TOKEN
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
                        addResponse(result)
                        val currentType = result

                        UserManager.userType = currentType


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


    fun uploadUserProfile() {
        chairRepository.uploadUserProfile {
            _showLottie.value = true
        }
    }
}