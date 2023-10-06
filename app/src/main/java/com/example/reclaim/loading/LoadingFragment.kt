package com.example.reclaim.loading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
        val sharePref = requireActivity().getSharedPreferences(
            "usermanager", Context.MODE_PRIVATE
        )


        Handler(Looper.getMainLooper()).postDelayed({
            if (sharePref?.getString("userid", null) == null) {
                findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToLoginFragment())
            } else {
                Log.i(TAG, "${sharePref.all}")

            }

        }, 3000)

        return binding.root
    }

    companion object{
        private const val TAG = "loadingpage"
    }

}