package com.example.reclaim.createprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentAiLoadingBinding


/**
 * A simple [Fragment] subclass.
 * Use the [AiLoadingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AiLoadingFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = FragmentAiLoadingBinding.inflate(inflater)
        return binding.root
    }


}