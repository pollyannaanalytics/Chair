package com.example.reclaim.profile

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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.reclaim.R
import com.example.reclaim.chatgpt.MessageToGPT
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.data.ReclaimDatabaseDao
import com.example.reclaim.databinding.FragmentProfileBinding
import com.example.reclaim.videocall.RTCActivity


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    val TAG = "PROFILE_PAGE"


    private var imageUri: Uri? = null
    private var imageList: MutableList<Uri?> = emptyList<Uri?>().toMutableList()


    lateinit var viewPager: ViewPager2
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(this.activity).application
        val dao = ReclaimDatabase.getInstance(application).reclaimDao()
        val factory = ProfileFactory(dao)
        val viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        // Inflate the layout for this fragment
        val binding = FragmentProfileBinding.inflate(inflater)

        viewPager = binding.chooseImgContent
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

        binding.chooseImgBtn.setOnClickListener {
            imageList = emptyList<Uri>().toMutableList()
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
            viewModel.sendDescriptionToGPT(worriesDescription)

        }

        viewModel.messageList.observe(viewLifecycleOwner) {
            if (it != emptyList<MessageToGPT>()) {
                viewModel.saveUserProfile(
                    username,
                    gender,
                    worriesDescription,
                    it.first().message.trim(),
                    imageList.map { it.toString() }
                )
            }

        }

        viewModel.readyToUploadOnFirebase.observe(viewLifecycleOwner) {
            if (it != false) {
                viewModel.uploadProfileToFirebase()
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToHomeFragment()
                )
            }
        }









        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data?.clipData != null) {
            val count = data.clipData!!.itemCount

            for (i in 0 until count) {
                imageUri = data.clipData!!.getItemAt(i).uri
                imageList.add(imageUri)

                Log.i(TAG, imageList.toString())
                try {
                    val adapter = ImageAdapter()
                    viewPager.adapter = adapter
                    adapter.submitList(imageList)

                    Log.i(TAG, "adapter submit successfully")
                }catch (e: Exception){
                    Log.e(TAG, e.toString())
                }


            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent()

        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(intent, 1)

    }
    private fun showPermissionRationaleDialog(){
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), READ_IMAGE_PERMISSION
            ) &&
            !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                READ_IMAGE_PERMISSION
            ), READ_IMAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun onImageReadPermissionDenied() {
        Toast.makeText(requireContext(), "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkImagePermission() {
        if(ContextCompat.checkSelfPermission(requireActivity(), READ_IMAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            requestReadImagesPermission()
        } else{
            pickImageFromGallery()
        }
    }
    companion object{
        private const val READ_IMAGE_PERMISSION_REQUEST_CODE = 1
        private const val READ_IMAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    }




}
