package com.example.reclaim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.source.ChairRepository

class HomeFactory(private val chairRepository: ChairRepository): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(chairRepository) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}