package com.example.reclaim.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class Friends(

    var id: Long = 0L,


    var userId: String,


    var userName: String,


    var imageUri: String,


    var chatRoomKey: String
): Parcelable
