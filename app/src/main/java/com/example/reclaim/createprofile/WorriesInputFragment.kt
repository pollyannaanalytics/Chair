package com.example.reclaim.createprofile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.FragmentWorriesInputBinding


/**
 * A simple [Fragment] subclass.
 * Use the [WorriesInputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WorriesInputFragment : Fragment() {
    private val TAG = "Worries"
    private val viewModel: WorriesInputViewModel by lazy {
        ViewModelProvider(this).get(WorriesInputViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentWorriesInputBinding.inflate(inflater)
        binding.viewModel = viewModel
        var worriesDescription = ""

        fun hideKeyboard() {
            val imm = this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.worriesEdit.windowToken, 0)
        }

        binding.progressBar.apply {
            max = 100
            progress = 80
        }
        binding.worriesEdit.doAfterTextChanged {
            worriesDescription = it.toString()
            binding.progressBar.progress += 20
        }

        binding.worriesEdit.setOnKeyListener{ _, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }

        binding.finishBtn.setOnClickListener {
            UserManager.worriesDescription = worriesDescription
            viewModel.uploadUserProfile()
            Log.i(TAG, "userManager: ${UserManager.worriesDescription}")
            findNavController().navigate(WorriesInputFragmentDirections.actionWorriesInputFragmentToHomeFragment())

        }


        return binding.root
    }


}