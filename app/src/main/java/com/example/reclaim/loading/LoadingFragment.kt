package com.example.reclaim.loading

import android.content.Context
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
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentLoadingBinding


/**
 * A simple [Fragment] subclass.
 * Use the [LoadingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoadingFragment : Fragment() {

    private lateinit var userManagerInSharePreference: String
    private lateinit var userIdInSharePreference: String
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
        userManagerInSharePreference = resources.getString(R.string.usermanager)
        userIdInSharePreference  = resources.getString(R.string.userid)
        val sharePref = requireActivity().getSharedPreferences(
            userManagerInSharePreference, Context.MODE_PRIVATE
        )
        binding.viewModel = viewModel


        viewModel.haveProfileInfoInFirebase.observe(viewLifecycleOwner) {
            if (it == true){
                Log.i(TAG, "already have info in firebase, go to home page")
                viewModel.putProfileInfoToUserManager()
                findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToHomeFragment())
            }else if (it == false){
                Log.i(TAG, "have token, but no user profile in firebase, should go to agreement")
                findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToAgreementFragment())

            }

        }

        Handler(Looper.getMainLooper()).postDelayed({
            val autId = sharePref.all.get(userIdInSharePreference).toString()
            if (autId == null || autId == "" || autId == "null") {
                findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToLoginFragment())
            } else {
                viewModel.loadProfileToUserManager(autId)
                Log.i(TAG, " auth id is : ${autId}")

            }

        }, 3000)

        return binding.root
    }

    companion object {
        private const val TAG = "loadingpage"
    }

}