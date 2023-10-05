package com.example.reclaim.profile

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
        binding.selfUsername.setText(UserManager.userName)
        binding.selfDescription.setText(UserManager.worriesDescription)
        binding.selfTag.setText("${UserManager.age + "yrs, " + UserManager.gender}")


        binding.friendNumberDescription.setText("目前有你有 ${UserManager.friendNumber} 個朋友! ")

        viewModel.touchPeople.observe(viewLifecycleOwner){
            binding.likeDescription.setText("目前你滑過 ${it} 個人! ")
        }


        binding.editProfile.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionAlreadySignUpProfileFragmentToProfileFragment())
        }

        return binding.root
    }


}