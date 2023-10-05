package com.example.reclaim.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao

class EditProfileFactory(private val dao: ReclaimDatabaseDao): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}