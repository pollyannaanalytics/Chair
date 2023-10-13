package com.example.reclaim.chatroom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.accessibility.AccessibilityViewCommand.ScrollToPositionArguments
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reclaim.R
import com.example.reclaim.data.Friends
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentChatRoomBinding
import com.example.reclaim.databinding.FragmentMeetingBinding
import com.example.reclaim.videocall.RTCActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.util.UUID


/**
 * A simple [Fragment] subclass.
 * Use the [ChatRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatRoomFragment : Fragment() {
    companion object {
        const val TAG = "messagechat"
    }

    private val arg by navArgs<ChatRoomFragmentArgs>()
    val db = Firebase.firestore




    lateinit var viewModel: ChatRoomViewModel


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = ChatRoomFactory(arg, databaseDao)
        viewModel = ViewModelProvider(this, factory).get(ChatRoomViewModel::class.java)
        val binding = FragmentChatRoomBinding.inflate(inflater)

        var sendText = ""
        val adapter = ChatRoomAdapter(
            ChatRoomAdapter.OnClickListener {
                Log.i(TAG, "join meeting ${it.meetingId}")
                viewModel.joinMeeting(it.meetingId)
                Log.i(TAG, "join meeting click")
            }
        )
        binding.viewModel = viewModel
        binding.chatRecordRecyclerview.adapter = adapter



        binding.messageEdit.doAfterTextChanged {
            sendText = it.toString()
        }

        binding.sendBtn.setOnClickListener {
            if (sendText != "") {
                Log.i(TAG, "send btn is clicked")
                viewModel.sendMessage(sendText, "message")
                binding.messageEdit.setText("")

            }


        }


        viewModel.recordWithFriend.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.chatRecordRecyclerview.scrollToPosition(it.size - 1)
            Log.i(TAG, "submit to adapter: $it")

        }

        binding.videoButton.setOnClickListener {
            val meetingId = UUID.randomUUID().leastSignificantBits.toString()

            val intent = Intent(requireActivity(), RTCActivity::class.java)
            intent.putExtra("meetingID", meetingId)
            intent.putExtra("isJoin", false)
            startActivity(intent)

            viewModel.sendVideoCallMessage(meetingId)


        }

        viewModel.joinMeetingId.observe(viewLifecycleOwner) {
            val intent = Intent(requireActivity(), RTCActivity::class.java)
            Log.i(TAG, "join meeting, id : $it")
            intent.putExtra("meetingID", it)
            intent.putExtra("isJoin", true)

            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()

        }






        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()

    }
}