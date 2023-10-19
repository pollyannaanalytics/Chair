package com.example.reclaim

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.reclaim.data.source.ChairRemoteDataSource
import com.example.reclaim.data.source.ChairRepository

class ServiceLocator {
    @Volatile
    var chairRepository: ChairRepository? = null
        @VisibleForTesting set

    fun provideTasksRepository(context: Context): ChairRepository {
        synchronized(this) {
            return chairRepository
                ?: chairRepository
                ?: createStylishRepository(context)
        }
    }


    private fun createStylishRepository(context: Context): ChairRepository{
        return ChairRepository(
            ChairRemoteDataSource()
        )
    }

}