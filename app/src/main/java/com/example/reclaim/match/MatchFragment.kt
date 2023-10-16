package com.example.reclaim.match

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reclaim.R
import com.example.reclaim.chatroom.ChatRoomFragmentArgs
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentMatchBinding


class MatchFragment : Fragment() {


    private lateinit var viewModel: MatchViewModel
    private val navArgs by navArgs<MatchFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val application = requireActivity().application
        val reclaimDatabaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = MatchFactory(navArgs, reclaimDatabaseDao)
        val binding = FragmentMatchBinding.inflate(inflater)
        viewModel = ViewModelProvider(this, factory).get(MatchViewModel::class.java)
        binding.viewModel = viewModel



        val leftAvatar = binding.selfContainer
        val rightAvatar = binding.otherContainer
        val matchTitle = binding.matchTitle
        var message = ""

        fun hideKeyboard() {
            val imm = this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.messageInputEdit.windowToken, 0)
        }

        avatarMatchAnimate(leftAvatar, rightAvatar)
        matchTitleAnimate(matchTitle)


        binding.messageInputEdit.doAfterTextChanged {
            message = it.toString()
        }


        binding.messageInputEdit.setOnKeyListener{ _, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }
        binding.sendSuccessfulHint.visibility = View.GONE

        binding.sendToChatRoom.setOnClickListener {
            if (binding.messageInputEdit.text.toString() != ""){
                viewModel.sendMessageToChatRoom(message)
                binding.messageInputEdit.setText("")
                binding.sendLottie.playAnimation()
                binding.matchLayout.alpha = 0.5f
                findNavController().navigate(MatchFragmentDirections.actionMatchFragmentToHomeFragment())
            }
        }


        binding.defaultMessage1.setOnClickListener {
            viewModel.sendMessageToChatRoom(binding.defaultMessage1.text.toString())

            binding.messageInputEdit.setText("")

            binding.sendLottie.playAnimation()
            binding.sendSuccessfulHint.visibility = View.VISIBLE
            binding.matchLayout.alpha = 0.5f
            findNavController().navigate(MatchFragmentDirections.actionMatchFragmentToHomeFragment())
        }


        binding.defaultMessage2.setOnClickListener {
            viewModel.sendMessageToChatRoom(binding.defaultMessage2.text.toString())
            binding.messageInputEdit.setText("")

            binding.sendLottie.playAnimation()
            binding.sendSuccessfulHint.visibility = View.VISIBLE
            binding.matchLayout.alpha = 0.5f
            findNavController().navigate(MatchFragmentDirections.actionMatchFragmentToHomeFragment())
        }

        binding.defaultMessage3.setOnClickListener {
            viewModel.sendMessageToChatRoom(binding.defaultMessage3.text.toString())
            binding.messageInputEdit.setText("")

            binding.sendLottie.playAnimation()
            binding.sendSuccessfulHint.visibility = View.VISIBLE
            binding.matchLayout.alpha = 0.5f
            findNavController().navigate(MatchFragmentDirections.actionMatchFragmentToHomeFragment())
        }




        binding.laterChatBtn.setOnClickListener {
            findNavController().navigate(MatchFragmentDirections.actionMatchFragmentToHomeFragment())
        }

        return binding.root
    }

    private fun matchTitleAnimate(matchTitle: TextView) {
        val scaleUpX = ObjectAnimator.ofFloat(
            matchTitle,
            "scaleX",
            0f,
            1f
        )

        val scaleUpY = ObjectAnimator.ofFloat(
            matchTitle,
            "scaleY",
            0f,
            1f
        )



        val animationSet = AnimatorSet()
        animationSet.playTogether(scaleUpX, scaleUpY)

        animationSet.duration = 1000
        animationSet.start()
    }

    fun avatarMatchAnimate(leftAvatar: CardView, rightAvatar: CardView) {

        val scaleUpX = ObjectAnimator.ofFloat(
            leftAvatar,
            "scaleX",
            1.0f,
            1.5f
        )

        val scaleUpY = ObjectAnimator.ofFloat(
            leftAvatar,
            "scaleY",
            1.0f,
            1.5f
        )

        val scaleDownX = ObjectAnimator.ofFloat(
            rightAvatar,
            "scaleX",
            1.0f, 1.5f
        )
        val scaleDownY = ObjectAnimator.ofFloat(
            rightAvatar,
            "scaleY",
            1.0f, 1.5f
        )


        val transitionSelfMoveAnimation = ObjectAnimator.ofFloat(
            leftAvatar,
            View.TRANSLATION_X,
            50f
        )

        val transitionOtherMoveAnimation = ObjectAnimator.ofFloat(
            rightAvatar,
            View.TRANSLATION_X,
            -50f
        )

        val scaleUpSet = AnimatorSet()
        scaleUpSet.playTogether(
            scaleUpX, scaleUpY
        )

        val scaleDownSet = AnimatorSet()
        scaleDownSet.playTogether(
            scaleDownX, scaleDownY
        )

        val animationSet = AnimatorSet()
        animationSet.playTogether(scaleUpSet, transitionSelfMoveAnimation, transitionOtherMoveAnimation, scaleDownSet)
        animationSet.duration = 1000

        animationSet.start()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}