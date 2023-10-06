package com.example.reclaim.createprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.FragmentAgreementBinding


/**
 * A simple [Fragment] subclass.
 * Use the [AgreementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AgreementFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentAgreementBinding.inflate(inflater)
        binding.usermanager = UserManager
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.agreeBtn.setOnClickListener {
            findNavController().navigate(com.example.reclaim.loading.AgreementFragmentDirections.actionAgreementFragmentToCreateProfileFragment())
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }


}