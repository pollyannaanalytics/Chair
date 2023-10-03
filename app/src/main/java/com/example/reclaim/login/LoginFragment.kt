package com.example.reclaim.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.FragmentLoginBinding
import com.example.reclaim.databinding.FragmentProfileBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentLoginBinding.inflate(inflater)
        binding.usermanager = UserManager
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val avatarBackground = binding.backgroundImageAvatar

        val scaleUpX = ObjectAnimator.ofFloat(avatarBackground, "scaleX", 1.0f, 1.15f)
        val scaleUpY = ObjectAnimator.ofFloat(avatarBackground, "scaleY", 1.0f, 1.15f)




        val alphaChange = ObjectAnimator.ofFloat(avatarBackground, "alpha", 0f, 1f)
        val scaleAnim = AnimatorSet()

        scaleAnim.playTogether(scaleUpX, scaleUpY, alphaChange)
        scaleAnim.duration = 5000

        scaleAnim.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                scaleAnim.start()
            }
        })
        scaleAnim.start()





        binding.continueBtn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
        }

        binding.loginAnotherBtn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToProfileFragment())
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }


}