package com.example.reclaim.editprofile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.chatgpt.MessageToGPT
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.data.UserManager
import com.example.reclaim.databinding.FragmentEditProfileBinding



/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    val TAG = "PROFILE_PAGE"


    private var imageUri: Uri? = UserManager.contentImage.toUri()

    lateinit var binding: FragmentEditProfileBinding
    lateinit var viewModel: ProfileViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(this.activity).application
        val dao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = EditProfileFactory(dao)
        val viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater)

//        viewPager = binding.chooseImgContent
        var userId = ""
        var username = ""
        var gender = ""
        var worriesDescription = ""





        binding.viewModel = viewModel
        binding.userManager = UserManager

        binding.ageEdit.doAfterTextChanged {
            userId = it.toString()
            Log.i(TAG,"userId: $it")
        }


        binding.usernameEdit.doAfterTextChanged {
            username = it.toString()
            Log.i(TAG,"userId: $it")
        }

        binding.worriesEdit.doAfterTextChanged {
            worriesDescription = it.toString()
            Log.i(TAG,"userId: $it")
        }

        binding.chooseImgBtn.setOnClickListener {
            checkImagePermission()
            pickImageFromGallery()
        }



        binding.genderGroup.setOnCheckedChangeListener { radioGroup, i ->
            gender = when (i) {
                R.id.male -> binding.male.text.toString()
                R.id.female -> binding.female.text.toString()
                R.id.third_gender -> binding.thirdGender.text.toString()
                else -> "the gender is not chose"
            }
        }



        binding.submitBtn.setOnClickListener {
            Log.i(TAG, "imageURI: $imageUri")
                UserManager.userId = userId
                UserManager.userName = username
                UserManager.gender = gender
                UserManager.worriesDescription = worriesDescription
                UserManager.userImage = imageUri.toString()

                viewModel.sendDescriptionToGPT(worriesDescription)
        }

        binding.cancelEditBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.messageList.observe(viewLifecycleOwner) {
            if (it != emptyList<MessageToGPT>()) {
                viewModel.saveUserProfile(
                    UserManager.userName,
                    gender,
                    worriesDescription,
                    it.first().message.trim(),
                    imageUri.toString()
                )
            }

        }

        viewModel.readyToUploadOnFirebase.observe(viewLifecycleOwner) {
            if (it != false) {
                viewModel.uploadImageToFireStorage(imageUri.toString())
                binding.finishLottie.playAnimation()

                findNavController().navigate(
                    EditProfileFragmentDirections.actionProfileFragmentToAlreadySignUpProfileFragment()
                )
            }
        }











        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                imageUri = data?.data
                if (null != imageUri) {
                    binding.userImage.setImageURI(imageUri)
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

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
