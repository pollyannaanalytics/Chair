package com.example.reclaim.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [UserInfo::class, UserProfile::class, ChatRecord::class, Relationship::class, Images::class, ChatRoomLocal::class], version = 1, exportSchema = false)

abstract class ReclaimDatabase: RoomDatabase() {

    abstract fun reclaimDao(): ReclaimDatabaseDao
    companion object{
        @Volatile
        private var INSTANCE: ReclaimDatabase? = null


        fun getInstance(context: Context): ReclaimDatabase{
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ReclaimDatabase::class.java,
                        "reclaim_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}