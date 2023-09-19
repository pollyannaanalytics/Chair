package com.example.reclaim.videocall

import android.util.Log
import android.util.Log.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignalingClient(
    private val meetingID : String,
    private val listener: SignalingClientListener
) : CoroutineScope{
companion object{
    private const val HOST_ADDRESS = "168.95.192.1"
}

    var jsonObject : JsonObject ?= null

    private val job = Job()

    val TAG = "SignallingClient"

    val db = Firebase.firestore

    private val gson = Gson()

    var SDPtype : String? = null

    override val coroutineContext = Dispatchers.IO + job

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    private fun connect() = launch {
        db.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }
        val sendData = sendChannel.trySend("").isSuccess

        sendData.let {
            Log.i(this@SignalingClient.javaClass.simpleName, "Sending: $it")
        }
        try {
            db.collection("voice_call")
                .document(meetingID)
                .addSnapshotListener{
                    snapshot, e ->
                    if(e != null){
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if(snapshot != null && snapshot.exists()){
                        val data = snapshot.data
                        if(data?. containsKey("type") !! &&
                            data.getValue("type").toString() == "OFFER"){
                            listener.onOfferReceived(SessionDescription(
                                SessionDescription.Type.OFFER, data["sdp"].toString()
                            ))
                            SDPtype = "Offer"
                        }else if(data ?.containsKey("type")!! &&
                            data.getValue("type").toString() == "ANSWER"){
                            listener.onAnswerReceived(SessionDescription(
                                SessionDescription.Type.ANSWER, data["sdp"].toString()
                            ))
                            SDPtype = "Answer"
                        }

                        Log.d(TAG, "Current data: ${snapshot.data}")
                    }else {
                        Log.d(TAG, "Current data: null")
                    }
                }

            db.collection("voice_call").document(meetingID)
                .collection("candidates").addSnapshotListener{
                    querysnapshot, e ->
                    if (e != null){
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if(querysnapshot != null && !querysnapshot.isEmpty){
                        for(dataSnapShot in querysnapshot){
                            val data = dataSnapShot.data
                            if (SDPtype == "Offer" && data.containsKey("type") && data.get("type")=="offerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString())
                                )


                            } else if (SDPtype == "Answer" && data.containsKey("type") && data.get("type")=="answerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString())
                                )
                            }
                            Log.e(TAG, "candidateQuery: $dataSnapShot" )
                        }
                    }
                }

        }
        catch (exception: Exception) {
            Log.e(TAG, "connectException: $exception")

        }
    }


    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        val type = when {
            isJoin -> "answerCandidate"
            else -> "offerCandidate"
        }
        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )
        db.collection("voice_call")
            .document("$meetingID").collection("candidates").document(type)
            .set(candidateConstant as Map<String, Any>)
            .addOnSuccessListener {
                e(TAG, "sendIceCandidate: Success" )
            }
            .addOnFailureListener {
                e(TAG, "sendIceCandidate: Error $it" )
            }
    }

    fun destroy() {
//        client.close()
        job.complete()
    }

}