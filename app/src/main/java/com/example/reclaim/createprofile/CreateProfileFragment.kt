package com.example.reclaim.createprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.FragmentCreateProfileBinding
import com.google.firebase.firestore.auth.User


/**
 * A simple [Fragment] subclass.
 * Use the [CreateProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateProfileFragment : Fragment() {

    val TAG = "CREATE_PROFILE_PAGE"
    var imageUri: Uri? = null
    lateinit var binding: FragmentCreateProfileBinding


    private val viewModel: CreateProfileViewModel by lazy {
        ViewModelProvider(this).get(CreateProfileViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateProfileBinding.inflate(inflater)
        binding.viewModel = viewModel
        val userManagerInSharedPreferences = resources.getString(R.string.usermanager)
        val userManagerIdInSharePreferences = resources.getString(R.string.userid)


        var username = ""
        var gender = "男"
        var worriesDescription = ""
        var userAge = ""
        var selfDescription = ""
        UserManager.worriesDescription = ""
        UserManager.userType = ""

        UserManager.userId =
            requireActivity().getSharedPreferences(
                userManagerInSharedPreferences,
                Context.MODE_PRIVATE
            ).all.get(userManagerIdInSharePreferences)
                .toString()

        binding.progressBar.apply {
            max = 100
            progress = 0
        }

        binding.chooseImgBtn.setOnClickListener {
            checkImagePermission()
            pickImageFromGallery()
        }

        binding.ageEdit.doAfterTextChanged {
            userAge = it.toString()
            if (userAge.isEmpty() || userAge == ""){
                binding.ageEdit.error = "此為必填項"
            }

            Log.i(TAG, "userAge: $it")
        }



        binding.ageEdit.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                binding.progressBar.progress = 10
                val imm =
                    this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.ageEdit.windowToken, 0)
                return@setOnKeyListener true
            }
            false
        }


        binding.usernameEdit.doAfterTextChanged {
            username = it.toString()
            if (username.isEmpty() || username == ""){
                binding.usernameEdit.error = "此為必填項"
            }
            Log.i(TAG, "userId: $it")
        }

        binding.usernameEdit.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                binding.progressBar.progress = 30
                val imm =
                    this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.usernameEdit.windowToken, 0)
                return@setOnKeyListener true
            }
            false
        }




        binding.selfDescriptionEdit.doAfterTextChanged {
            selfDescription = it.toString()
            if (selfDescription.isEmpty() || selfDescription == ""){
                binding.selfDescription.error = "此為必填項"
            }else{
                binding.progressBar.progress = 80
            }


        }



        binding.nextMove.setOnClickListener {
            if (userAge.isNotEmpty() && username.isNotEmpty() && gender.isNotEmpty() && selfDescription.isNotEmpty() && imageUri.toString().isNotEmpty()){
                UserManager.age = userAge
                UserManager.userName = username
                UserManager.gender = gender
                UserManager.selfDescription = selfDescription
                binding.nextMove.isEnabled = false

                viewModel.uploadImageToFireStorage(imageUri.toString())

                Log.i(TAG, "$UserManager")


                findNavController().navigate(CreateProfileFragmentDirections.actionCreateProfileFragmentToWorriesInputFragment())
            }else{

            }


        }



        binding.genderGroup.setOnCheckedChangeListener { radioGroup, i ->
            binding.progressBar.progress = 80
            gender = when (i) {
                R.id.male -> binding.male.text.toString()
                R.id.female -> binding.female.text.toString()
                R.id.third_gender -> binding.thirdGender.text.toString()
                else -> binding.male.text.toString()
            }
        }

        return binding!!.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                imageUri = data?.data
                if (null != imageUri) {
                    binding?.userImage?.setImageURI(imageUri)
                }
            }
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent()

        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)


    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Album Permission Required")
            .setMessage("This app need use your album")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                (true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onImageReadPermissionDenied()
            }
            .show()
    }


    private fun requestReadImagesPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), READ_IMAGE_PERMISSION
            ) &&
            !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    READ_IMAGE_PERMISSION
                ), READ_IMAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun onImageReadPermissionDenied() {
        Toast.makeText(requireContext(), "Camera and Audio Permission Denied", Toast.LENGTH_LONG)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkImagePermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                READ_IMAGE_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadImagesPermission()
        } else {
            pickImageFromGallery()
        }
    }

    companion object {
        private const val READ_IMAGE_PERMISSION_REQUEST_CODE = 1
        private const val READ_IMAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val SELECT_PICTURE = 200
        const val USER_MANAGER = "UserManager"
    }

}