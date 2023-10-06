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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentLoadingBinding
import com.example.reclaim.login.LoginViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [LoadingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoadingFragment : Fragment() {
    val UserManagerInSharePreference = resources.getString(R.string.usermanager)
    val UserIdInSharePreference = resources.getString(R.string.userid)
    private val viewModel: LoadingViewModel by lazy {
        ViewModelProvider(this).get(LoadingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentLoadingBinding.inflate(inflater)
        binding.loadingAnimation.playAnimation()
        val sharePref = requireActivity().getSharedPreferences(
            UserManagerInSharePreference, Context.MODE_PRIVATE
        )
        binding.viewModel = viewModel


        viewModel.userProfile.observe(viewLifecycleOwner) {
            viewModel.putProfileInfoToUserManager(it)
            findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToHomeFragment())
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val autId = sharePref.all.get(UserIdInSharePreference).toString()
            if (autId == null || autId == "") {
                findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToLoginFragment())
            } else {
                viewModel.loadProfileToUserManager(autId)
                Log.i(TAG, "${autId}")

            }

        }, 3000)

        return binding.root
    }

    companion object {
        private const val TAG = "loadingpage"
    }

}