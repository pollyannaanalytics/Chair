package com.example.reclaim.createprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reclaim.data.source.ChairRepository

class WorriesInputFactory(
    private val chairRepository: ChairRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorriesInputViewModel::class.java)) {
            return WorriesInputViewModel(chairRepository) as T
        }

        throw IllegalArgumentException("Unknown viewModel class")
    }
}