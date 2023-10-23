package com.example.reclaim.createprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.source.ChairRepository

class CreateProfileFactory(
    private val chairRepository: ChairRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateProfileViewModel::class.java)) {
            return CreateProfileViewModel(chairRepository) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}