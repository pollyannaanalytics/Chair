package com.example.reclaim.loading

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentLoadingBinding


/**
 * A simple [Fragment] subclass.
 * Use the [LoadingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoadingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentLoadingBinding.inflate(inflater)
        binding.loadingAnimation.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToAgreementFragment())
        }, 2000)

        return binding.root
    }

}