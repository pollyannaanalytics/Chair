package com.example.reclaim.profile

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.example.reclaim.data.UserManager

import com.example.reclaim.databinding.FragmentAlreadySignUpProfileBinding



/**
 * A simple [Fragment] subclass.
 * Use the [AlreadySignUpProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlreadySignUpProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentAlreadySignUpProfileBinding.inflate(inflater)
        binding.selfAvatorImg.setImageURI(UserManager.userImage.toUri())
        binding.selfUsername.setText(UserManager.userName)
        binding.selfDescription.setText(UserManager.worriesDescription)
        binding.selfTag.setText("${UserManager.age + "yrs, " + UserManager.gender}")

        return binding.root
    }


}