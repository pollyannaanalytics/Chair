package com.example.reclaim.profile

import android.content.Context
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.data.UserManager


import com.example.reclaim.databinding.FragmentProfileBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentProfileBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.usermanager = UserManager


        binding.infoIcon.setOnClickListener {
            if (binding.selfCard.visibility == View.GONE){
                binding.selfCard.visibility = View.VISIBLE
            }else{
                binding.selfCard.visibility = View.GONE
            }
        }



        binding.editIcon.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionAlreadySignUpProfileFragmentToProfileFragment2())
        }





        return binding.root
    }


}