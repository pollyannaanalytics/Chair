package com.example.reclaim.createprofile

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.source.ChairRemoteDataSource
import com.example.reclaim.data.source.ChairRepository
import com.example.reclaim.databinding.FragmentWorriesInputBinding


/**
 * A simple [Fragment] subclass.
 * Use the [WorriesInputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WorriesInputFragment : Fragment() {


    companion object{
        private const val TAG = "Worries"
        private const val TOTAL_PROGRESS = 100
        private const val CURRENT_PROGRESS = 80
        private const val ADD_PROGRESS = 20
        private const val FLAG = 0
        private const val LAYOUT_ALPHA_START = 1f
        private const val LAYOUT_ALPHA_FINISH = 0.5f
        private const val DELAY_1000_MILLS = 1000L
    }

    lateinit var viewModel: WorriesInputViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentWorriesInputBinding.inflate(inflater)

        val factory = WorriesInputFactory(ChairRepository(ChairRemoteDataSource()))

        viewModel = ViewModelProvider(this, factory)[WorriesInputViewModel::class.java]

        binding.worriesLayout.alpha = LAYOUT_ALPHA_START

        binding.viewModel = viewModel

        var worriesDescription = ""

        fun hideKeyboard() {
            val imm =
                this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.worriesEdit.windowToken, FLAG)
        }

        binding.progressBar.apply {
            max = TOTAL_PROGRESS
            progress = CURRENT_PROGRESS
        }
        binding.worriesEdit.doAfterTextChanged {
            worriesDescription = it.toString()
            binding.progressBar.progress += ADD_PROGRESS
        }

        binding.worriesEdit.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }

        binding.finishBtn.setOnClickListener {
            it.isEnabled = false
            binding.worriesLayout.alpha = LAYOUT_ALPHA_FINISH
            UserManager.worriesDescription = worriesDescription
            viewModel.sendDescriptionToGPT(UserManager.worriesDescription)
            Log.i(TAG, "userManager: ${UserManager.worriesDescription}")


        }
        viewModel.messageList.observe(viewLifecycleOwner) {
            if (UserManager.userType != "") {
                viewModel.uploadUserProfile()
            }

        }
        viewModel.showLottie.observe(viewLifecycleOwner) {
            if (it == true) {


                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.loadingAnimation.cancelAnimation()
                        binding.successfullyAnimation.playAnimation()
                        findNavController().navigate(WorriesInputFragmentDirections.actionWorriesInputFragmentToAiLoadingFragment())
                    }, DELAY_1000_MILLS
                )


            }
        }






        return binding.root
    }


}