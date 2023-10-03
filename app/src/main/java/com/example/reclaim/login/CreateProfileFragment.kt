package com.example.reclaim.login

import android.Manifest
import android.app.Activity
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
import androidx.core.widget.doAfterTextChanged
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentCreateProfileBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateProfileFragment : Fragment() {

    val TAG = "CREATE_PROFILE_PAGE"
    var imageUri : Uri? = null
    lateinit var binding: FragmentCreateProfileBinding
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         binding = FragmentCreateProfileBinding.inflate(inflater)

        var userId = ""
        var username = ""
        var gender = ""
        var worriesDescription = ""

        binding.progressBar.apply {
            max = 100
            progress = 0
        }

        binding.idEdit.doAfterTextChanged {
            userId = it.toString()
            Log.i(TAG,"userId: $it")
            binding.progressBar.progress = 20
        }


        binding.usernameEdit.doAfterTextChanged {
            username = it.toString()
            Log.i(TAG,"userId: $it")
            binding.progressBar.progress = 40
        }


        binding.chooseImgBtn.setOnClickListener {
            checkImagePermission()
            pickImageFromGallery()
            binding.progressBar.progress = 60
        }



        binding.genderGroup.setOnCheckedChangeListener { radioGroup, i ->
            binding.progressBar.progress = 80
            gender = when (i) {
                R.id.male -> binding.male.text.toString()
                R.id.female -> binding.female.text.toString()
                R.id.third_gender -> binding.thirdGender.text.toString()
                else -> "the gender is not chose"
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

    }

}