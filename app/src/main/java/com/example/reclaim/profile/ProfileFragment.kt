package com.example.reclaim.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.chatgpt.MessageToGPT
import com.example.reclaim.databinding.FragmentProfileBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    val TAG = "PROFILE_PAGE"

    private val viewModel: ProfileViewModel by lazy { ViewModelProvider(this).get(ProfileViewModel::class.java)}
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentProfileBinding.inflate(inflater)
        var username = ""
        var gender = ""
        var worriesDescription = ""

        binding.viewModel = viewModel

        binding.usernameEdit.doAfterTextChanged {
            username = it.toString()
        }

        binding.worriesEdit.doAfterTextChanged {
            worriesDescription = it.toString()
        }



        binding.genderGroup.setOnCheckedChangeListener { radioGroup, i ->
            gender = when(i){
                R.id.male -> R.id.male.toString()
                R.id.female -> R.id.female.toString()
                R.id.third_gender -> R.id.third_gender.toString()
                else -> "the gender is not chose"
            }
        }


        binding.submitBtn.setOnClickListener {
            viewModel.sendDescriptionToGPT(worriesDescription)

        }

        viewModel.messageList.observe(viewLifecycleOwner) {
            if(it != emptyList<MessageToGPT>()){
                viewModel.saveUserProfile(username, gender, worriesDescription, it.first().message.trim())
            }

        }

        viewModel.readyToUploadOnFirebase.observe(viewLifecycleOwner){
            if(it!= false){
                viewModel.uploadProfileToFirebase()
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToHomeFragment()
                )
            }
        }







        return binding.root
    }
}