package com.example.reclaim.chatlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentChatListBinding



/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = ChatListFactory(databaseDao)
        val viewModel = ViewModelProvider(this, factory).get(ChatListViewModel::class.java)
        val binding = FragmentChatListBinding.inflate(inflater)
        binding.viewModel = viewModel


        val adapter = ChatListAvatorAdapter(ChatListAvatorAdapter.OnClickListener {
            viewModel.navigateToChatRoom
        })
        binding.friendsRecylerview.adapter = adapter




        viewModel.friendsList.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.submitList(it)
            }

        }

        viewModel.navigateToChatRoom.observe(viewLifecycleOwner) {
            findNavController().navigate(ChatListFragmentDirections.actionChatListFragmentToChatRoomFragment(it))
        }

        return binding.root
    }

}