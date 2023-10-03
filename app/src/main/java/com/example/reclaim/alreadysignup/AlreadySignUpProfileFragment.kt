package com.example.reclaim.alreadysignup

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.MainViewModel
import com.example.reclaim.data.UserManager

import com.example.reclaim.databinding.FragmentAlreadySignUpProfileBinding


/**
 * A simple [Fragment] subclass.
 * Use the [AlreadySignUpProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlreadySignUpProfileFragment : Fragment() {

    private val viewModel: AlreadySignUpViewModel by lazy {
        ViewModelProvider(this).get(AlreadySignUpViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentAlreadySignUpProfileBinding.inflate(inflater)
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
            findNavController().navigate(AlreadySignUpProfileFragmentDirections.actionAlreadySignUpProfileFragmentToProfileFragment())
        }

        return binding.root
    }


}