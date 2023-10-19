package com.example.reclaim

import android.app.Application
import com.example.reclaim.data.source.ChairRepository
import kotlin.properties.Delegates

class ChairApplication: Application() {

    val chairRepository: ChairRepository
        get() = ServiceLocator.provideTasksRepository(this)


    companion object{
        var instance: ChairApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}