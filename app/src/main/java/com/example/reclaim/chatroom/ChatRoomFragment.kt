package com.example.reclaim.chatroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentChatRoomBinding
import com.example.reclaim.databinding.FragmentMeetingBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatRoomFragment : Fragment() {
    companion object{
        const val TAG = "messagechat"
    }

val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentChatRoomBinding.inflate(inflater)
        var sendText = ""

        binding.messageEdit.doAfterTextChanged {
            sendText = it.toString()
        }

        binding.sendBtn.setOnClickListener {
            val records = FirebaseFirestore.getInstance().collection("record_table")
            val document = records.document()

            val data = hashMapOf(
                "project_id" to "pinyun",
                "record_id" to UUID.randomUUID(),
                "user_id" to "pinyun",
                "content" to sendText
            )



            db.collection("record_table").add(data).addOnSuccessListener {

                it.update("send_time", ServerValue.REQUEST_TIME)
                Log.i(TAG, "DocumentSnapshot added with ID: ${it}")
            }
                .addOnFailureListener {
                    e ->   Log.w(TAG, "Error happen: $e")
                }
        }



        db.collection("record_table").orderBy("send_time", Query.Direction.ASCENDING).addSnapshotListener{
            snapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                Log.w(TAG, "Error happen: $firebaseFirestoreException")
            }
            else{
                Log.i(TAG, "DocumentSnapshot added with ID: ${snapshot?.documents}")
            }
        }





        return binding.root
    }


}