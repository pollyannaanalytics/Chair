package com.example.reclaim.createprofile

import androidx.lifecycle.ViewModel
import com.example.reclaim.data.source.ChairRepository


class CreateProfileViewModel(
    private val repository: ChairRepository
): ViewModel() {

    companion object{
        private const val TAG = "CreateProfile"

    }

    fun uploadImageToFireStorage(stringOfUri: String) {

        repository.uploadImageToFireStorage(stringOfUri)

    }
}