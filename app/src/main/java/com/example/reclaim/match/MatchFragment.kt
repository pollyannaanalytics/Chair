package com.example.reclaim.match

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.reclaim.R
import com.example.reclaim.chatroom.ChatRoomFragmentArgs
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentMatchBinding


class MatchFragment : Fragment() {


    private lateinit var viewModel: MatchViewModel
    private val navArgs by navArgs<MatchFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val application = requireActivity().application
        val reclaimDatabaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = MatchFactory(navArgs, reclaimDatabaseDao)
        val binding = FragmentMatchBinding.inflate(inflater)
        viewModel = ViewModelProvider(this, factory).get(MatchViewModel::class.java)
        binding.viewModel = viewModel





        return binding.root
    }


}