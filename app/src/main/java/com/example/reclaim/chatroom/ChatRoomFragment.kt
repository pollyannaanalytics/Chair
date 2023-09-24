package com.example.reclaim.chatroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.reclaim.R
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentChatRoomBinding
import com.example.reclaim.databinding.FragmentMeetingBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import java.util.UUID



/**
 * A simple [Fragment] subclass.
 * Use the [ChatRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatRoomFragment : Fragment() {
    companion object{
        const val TAG = "messagechat"
    }

    private val arg by navArgs<ChatRoomFragmentArgs>()
val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentChatRoomBinding.inflate(inflater)
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = ChatRoomFactory(arg, databaseDao)
        val viewModel = ViewModelProvider(this, factory).get(ChatRoomViewModel::class.java)
        var sendText = ""

        binding.viewModel = viewModel

        binding.messageEdit.doAfterTextChanged {
            sendText = it.toString()
        }

        binding.sendBtn.setOnClickListener {

        }






        return binding.root
    }


}