package com.example.reclaim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.reclaim.databinding.FragmentMeetingBinding
import com.example.reclaim.videocall.Constants
import com.example.reclaim.videocall.RTCActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MeetingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeetingFragment : Fragment() {
    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =  FragmentMeetingBinding.inflate(inflater)


                Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        binding.startMeeting.setOnClickListener {
//            if (binding.meetingId.text.toString().trim().isNullOrEmpty())
//                binding.meetingId.error = "Please enter meeting id"
//            else
            db.collection("voice_call")
                .document(binding.meetingId.text.toString())
                .get()
                .addOnSuccessListener {
                    if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                        binding.meetingId.error = "Please enter new meeting ID"
                    } else {
                        val intent = Intent(requireActivity(), RTCActivity::class.java)
                        intent.putExtra("meetingID",binding.meetingId.text.toString())
                        intent.putExtra("isJoin",false)
                        startActivity(intent)
                    }



                }
                .addOnFailureListener {
                    Log.i("start_voice_call", it.toString())
                    binding.meetingId.error = "Please enter new meeting ID"
                }
//            }
        }
        binding.joinMeeting.setOnClickListener {
            if (binding.joinMeeting.text.toString().trim().isNullOrEmpty())
                binding.joinMeeting.error = "Please enter meeting id"
            else {
                val intent = Intent(requireActivity(), RTCActivity::class.java)
                intent.putExtra("meetingID",binding.meetingId.text.toString())
                intent.putExtra("isJoin",true)
                startActivity(intent)
            }
        }


        return binding.root
    }


}