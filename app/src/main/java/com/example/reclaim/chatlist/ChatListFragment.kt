package com.example.reclaim.chatlist

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentChatListBinding



/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {

    private val TAG = "ChatListFragment"


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = ChatListFactory(databaseDao, requireActivity())
        val viewModel = ViewModelProvider(this, factory).get(ChatListViewModel::class.java)
        val binding = FragmentChatListBinding.inflate(inflater)
        binding.viewModel = viewModel
        val avatarAdapter = ChatListAvatorAdapter(ChatListAvatorAdapter.OnClickListener{
            viewModel.displayChatRoom(it)
            Log.i(TAG, "click on $it")
        })

        val recordAdapter = ChatListRecordAdapter(ChatListRecordAdapter.OnClickListener{
            viewModel.displayChatRoom(it)
            Log.i(TAG, "click on $it")
        })

        binding.friendsRecylerview.adapter = avatarAdapter

        binding.chatRecordRecyclerview.adapter = recordAdapter


        viewModel.recordList.observe(viewLifecycleOwner){
            if(it != null && it.size != 0){
                avatarAdapter.submitList(it)
                recordAdapter.submitList(it)
                Log.i(TAG, "friend in recyclerview: $it")
            }



        }




//        viewModel.friendsList.observe(viewLifecycleOwner) {
//
//            val adapter = ChatListAvatorAdapter(ChatListAvatorAdapter.OnClickListener {
//                    viewModel.displayChatRoom(it)
//                    Log.i(TAG, "click on $it")
//                })
//                binding.friendsRecylerview.adapter = adapter
//            if(it != null && it.size != 0){
//                adapter.submitList(it)
//
//            }
//
//
//
//
//
//        }

        viewModel.navigateToChatRoom.observe(viewLifecycleOwner) {
            if(it != null){
                findNavController().navigate(ChatListFragmentDirections.actionChatListFragmentToChatRoomFragment(it))
                viewModel.navigateToRoom()
            }

        }

        return binding.root
    }



}