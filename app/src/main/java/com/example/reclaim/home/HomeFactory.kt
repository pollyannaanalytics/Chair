package com.example.reclaim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.ReclaimDatabaseDao

class HomeFactory(private val dao: ReclaimDatabaseDao): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}